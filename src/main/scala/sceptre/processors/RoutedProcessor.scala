package sceptre.processors

import sceptre.plumbing.Msg.StringMsg
import sceptre.plumbing.{Envelope, Outbound, Inbound, Route, Msg}


abstract class RoutedProcessor(route: Route) extends Processor {
  def sendBack(msg: Msg): Unit = route match {
    case Inbound => sendOutbound(msg)
    case Outbound => sendInbound(msg)
  }

  def send(msg: Msg): Unit = route match {
    case Inbound => sendInbound(msg)
    case Outbound => sendOutbound(msg)
  }

  def send(str: String): Unit = send(StringMsg(str))

  override def process = {
    case Envelope(msg, `route`) => processMsg(msg)
    case x => passthru(x)
  }

  def processMsg(msg: Msg): Unit
}
