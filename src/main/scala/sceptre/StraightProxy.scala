package sceptre

import java.net.InetSocketAddress

import akka.actor._
import org.slf4j.LoggerFactory
import sceptre.plumbing.{TelnetClient, TelnetServer}
import sceptre.processors.MessageLogger
import sceptre.protocol.TelnetCapability._

import sceptre.protocol.{TelnetCapability, TelnetNegotiator}
import sceptre.protocol.TelnetCodes._
import sceptre.plumbing.Pipable._

object StraightProxy extends App {

  private val log = LoggerFactory.getLogger(this.getClass)

  log.info("app starting")


  implicit val system: ActorSystem = ActorSystem.create()
  val mudFacingNegotiator = TelnetNegotiator(
      "mud",
      Reject(SUPPRESS_GO_AHEAD),
      Refuse(SUPPRESS_GO_AHEAD),
      Refuse(ECHO),
      Reject(ECHO),
      Refuse(NAWS),
      Reject(NAWS)
//    Passthrough(MSSP),
//    Passthrough(ECHO),
//    Passthrough(TERMINAL_TYPE),
//    Passthrough(TERMINAL_SPEED),
//    Passthrough(X_DISPLAY),
//    Passthrough(NEW_ENV),
//    Passthrough(ENVVARS),
//    Passthrough(NAWS),
//    Passthrough(STATUS),
//    Passthrough(TOGGLE_FLOW_CONTROL),
//    Passthrough(TRANSMIT_BINARY),
//    Passthrough(TIMING_MARK),
//    Passthrough(LINEMODE)
  )

  val localFacingNegotiator = TelnetNegotiator(
    "client",
    Proffer(TRANSMIT_BINARY),
    Solicit(TRANSMIT_BINARY),
    Proffer(MXP),
    Proffer(GMCP),
    Solicit(TERMINAL_TYPE),
    Solicit(NAWS),
    Solicit(ENVVARS),
    Solicit(STATUS),
    Solicit(NEW_ENV),
    Reject(SUPPRESS_GO_AHEAD),
    Refuse(SUPPRESS_GO_AHEAD),
    Reject(TERMINAL_SPEED),
    Refuse(TERMINAL_SPEED),
    Refuse(ECHO),
    Reject(ECHO)
//    Passthrough(X_DISPLAY),
//    Passthrough(TOGGLE_FLOW_CONTROL),
//    Passthrough(TIMING_MARK),
//    Passthrough(LINEMODE)
  )


  val inboundServerAddr = new InetSocketAddress("localhost", 7777)
  val inboundServer = new TelnetServer("local-facing", inboundServerAddr)

  val mudAddr = new InetSocketAddress("localhost", 23)
  //val tcpClientAddr = new InetSocketAddress("avalon-rpg.com", 23)
  val mudEndpoint = new TelnetClient("mud-facing", mudAddr)

//  val x = FnToObservableResponseIsPipable(outboundNegotiator.wire)

  val inboundConnections = inboundServer.start
  for (inboundConn <- inboundConnections) {
    val mudConnections = mudEndpoint.start
    for(mudConn <- mudConnections) {
      val wireForward = mudConn >>
        MessageLogger("mud") >>
        mudFacingNegotiator

      val wireBackward = inboundConn >>
        MessageLogger("client") >>
        localFacingNegotiator

      wireForward >> wireBackward
      wireBackward >> wireForward
    }
  }


  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  system.shutdown()
}


