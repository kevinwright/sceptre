package sceptre.plumbing

import java.net.InetSocketAddress
import akka.actor.ActorSystem
import org.slf4j.LoggerFactory
import rx.lang.scala._
import sceptre.protocol.{TelnetMuxer, TelnetDemuxer}

import Endpoint._

private[plumbing] class TelnetEndpoint(endpointType: EndpointType, name: String, addr: InetSocketAddress)(implicit system: ActorSystem) {
  private val log = LoggerFactory.getLogger(this.getClass)

  val tcpEndpoint = new TcpEndpoint(endpointType, name, addr)

  def start(): Observable[Terminus[Msg]] = {

    tcpEndpoint.start map {
      case tcpConn => new Terminus[Msg] {
        val demux = new TelnetDemuxer()
        val mux = new TelnetMuxer()

        val sink = (source: Observable[Msg]) => {
          val muxed = source.map{ msg =>
            println(s"$name output ${msg.toDebugString}")
            msg
          }.concatMap(mux).map{ bytestr =>
            println(name + " output bytes " + bytestr.iterator.map(_.toInt & 0xFF).mkString("[", ",", "]"))
            bytestr
          }
          tcpConn.sink(muxed)
        }

        val source: Observable[Msg] =
          tcpConn.source.map{bytestr =>
            println(name + " input bytes " + bytestr.iterator.map(_.toInt & 0xFF).mkString("[", ",", "]"))
            bytestr
          }.concatMap(demux).map{ msg =>
            println(name + " input " + msg.toDebugString)
            msg
          }

        val connect = tcpConn.connect
      }
    }
  }
}

class TelnetClient(name: String, addr: InetSocketAddress)(implicit system: ActorSystem)
  extends TelnetEndpoint(Client, name, addr)

class TelnetServer(name: String, addr: InetSocketAddress)(implicit system: ActorSystem)
  extends TelnetEndpoint(Server, name, addr)
