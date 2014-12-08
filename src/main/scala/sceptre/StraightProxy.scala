package sceptre

import java.net.InetSocketAddress

import akka.actor._
import sceptre.plumbing.Msg._
import sceptre.processors._
import sceptre.protocol.TelnetCapability.Passthrough
import sceptre.protocol.{TelnetNegotiator, TelnetCodes}
import TelnetCodes._
import sceptre.plumbing.{Outbound, Inbound, Pipeline}

object StraightProxy extends App {
  implicit val system: ActorSystem = ActorSystem.create()
  val inboundNegotiator = TelnetNegotiator(
    Inbound,
    Passthrough(MSSP),
    Passthrough(ECHO),
    Passthrough(TERMINAL_TYPE),
    Passthrough(TERMINAL_SPEED),
    Passthrough(X_DISPLAY),
    Passthrough(NEW_ENV),
    Passthrough(ENVVARS),
    Passthrough(NAWS),
    Passthrough(STATUS),
    Passthrough(SUPPRESS_GO_AHEAD),
    Passthrough(TOGGLE_FLOW_CONTROL),
    Passthrough(TRANSMIT_BINARY),
    Passthrough(TIMING_MARK),
    Passthrough(LINEMODE)
  )

  val outboundNegotiator = TelnetNegotiator(
    Outbound,
    Passthrough(MXP),
    Passthrough(MSDP),
    Passthrough(GMCP),
    Passthrough(ECHO),
    Passthrough(TERMINAL_TYPE),
    Passthrough(TERMINAL_SPEED),
    Passthrough(X_DISPLAY),
    Passthrough(NEW_ENV),
    Passthrough(ENVVARS),
    Passthrough(NAWS),
    Passthrough(STATUS),
    Passthrough(SUPPRESS_GO_AHEAD),
    Passthrough(TOGGLE_FLOW_CONTROL),
    Passthrough(TRANSMIT_BINARY),
    Passthrough(TIMING_MARK),
    Passthrough(LINEMODE)
  )


  val pipeline = new Pipeline(
    name         = "pipeline",
    clientAddr   = new InetSocketAddress("localhost", 7777),
    serverAddr   = new InetSocketAddress("localhost", 23),
//    serverAddr   = new InetSocketAddress("avalon-rpg.com", 23),
    processBuilders = Seq(
      outboundNegotiator,
      MessageLogger(classOf[StringMsg], classOf[TelnetSeqMsg], classOf[MxpEscape]),
      ProtocolDebugger(classOf[TelnetSeqMsg]),
      inboundNegotiator
    )
  )

  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  system.shutdown()
}


