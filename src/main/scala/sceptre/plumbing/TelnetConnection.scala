package sceptre.plumbing

import java.net.InetSocketAddress
import akka.actor.ActorSystem
import akka.util.ByteString
import org.slf4j.LoggerFactory
import rx.lang.scala._
import sceptre.protocol.{TelnetMuxer, TelnetDemuxer}

import Endpoint._

private[plumbing] class TelnetEndpoint(endpointType: EndpointType, name: String, addr: InetSocketAddress)(implicit system: ActorSystem) {
  private val log = LoggerFactory.getLogger(this.getClass)

  val tcpEndpoint = new TcpEndpoint(endpointType, name, addr)

  def stringify(bytestr: ByteString): String = bytestr.iterator.map(_.toInt & 0xFF).mkString("[", ",", "]")

  def logInBytes(bytestr: ByteString): ByteString = { println(name + " input bytes " + stringify(bytestr)); bytestr }
  def logOutBytes(bytestr: ByteString): ByteString = { println(name + " output bytes " + stringify(bytestr)); bytestr }
  def logInMsg(msg: Msg): Msg = { println(name + " input " + msg.toDebugString); msg }
  def logOutMsg(msg: Msg): Msg = { println(name + " output " + msg.toDebugString); msg }

  def start(): Observable[Terminus[Msg]] = {

    tcpEndpoint.start map {
      case tcpConn => new Terminus[Msg] {
        val demux = new TelnetDemuxer()
        val mux = new TelnetMuxer()

        val sink = (source: Observable[Msg]) => {
          //val muxed = source map logOutMsg concatMap mux map logOutBytes
          val muxed = source map logOutMsg concatMap mux
          tcpConn.sink(muxed)
        }

        val source: Observable[Msg] =
          tcpConn.source concatMap demux map logInMsg
          //tcpConn.source map logInBytes concatMap demux map logInMsg

        val connect = tcpConn.connect
      }
    }
  }
}

class TelnetClient(name: String, addr: InetSocketAddress)(implicit system: ActorSystem)
  extends TelnetEndpoint(Client, name, addr)

class TelnetServer(name: String, addr: InetSocketAddress)(implicit system: ActorSystem)
  extends TelnetEndpoint(Server, name, addr)
