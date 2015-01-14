package sceptre

import java.net.InetSocketAddress

import akka.actor._
import org.slf4j.LoggerFactory
import sceptre.plumbing.{TelnetClient, TelnetServer}

import sceptre.protocol.TelnetNegotiator
import sceptre.plumbing.Pipable._

object StraightProxy extends App {

  private val log = LoggerFactory.getLogger(this.getClass)

  log.info("app starting")


  implicit val system: ActorSystem = ActorSystem.create()
  val inboundNegotiator = TelnetNegotiator(
//    Passthrough(MSSP),
//    Passthrough(ECHO),
//    Passthrough(TERMINAL_TYPE),
//    Passthrough(TERMINAL_SPEED),
//    Passthrough(X_DISPLAY),
//    Passthrough(NEW_ENV),
//    Passthrough(ENVVARS),
//    Passthrough(NAWS),
//    Passthrough(STATUS),
//    Passthrough(SUPPRESS_GO_AHEAD),
//    Passthrough(TOGGLE_FLOW_CONTROL),
//    Passthrough(TRANSMIT_BINARY),
//    Passthrough(TIMING_MARK),
//    Passthrough(LINEMODE)
  )

  val outboundNegotiator = TelnetNegotiator(
//    Passthrough(MXP),
//    Passthrough(MSDP),
//    Passthrough(GMCP),
//    Passthrough(ECHO),
//    Passthrough(TERMINAL_TYPE),
//    Passthrough(TERMINAL_SPEED),
//    Passthrough(X_DISPLAY),
//    Passthrough(NEW_ENV),
//    Passthrough(ENVVARS),
//    Passthrough(NAWS),
//    Passthrough(STATUS),
//    Passthrough(SUPPRESS_GO_AHEAD),
//    Passthrough(TOGGLE_FLOW_CONTROL),
//    Passthrough(TRANSMIT_BINARY),
//    Passthrough(TIMING_MARK),
//    Passthrough(LINEMODE)
  )


  val tcpServerAddr = new InetSocketAddress("localhost", 7777)
  val tcpServer = new TelnetServer("telnetserver", tcpServerAddr)

  val tcpClientAddr = new InetSocketAddress("localhost", 23)
  //val tcpClientAddr = new InetSocketAddress("avalon-rpg.com", 23)
  val tcpClient = new TelnetClient("telnetclient", tcpClientAddr)

//  val x = FnToObservableResponseIsPipable(outboundNegotiator.wire)

  val serverConnections = tcpServer.start
  for (serverConn <- serverConnections) {
    val clientConnections = tcpClient.start
    for(clientConn <- clientConnections) {
      clientConn >> outboundNegotiator.wire >> serverConn
      serverConn >> inboundNegotiator.wire >> clientConn
    }
  }


  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  system.shutdown()
}


