package sceptre.processors

import sceptre.plumbing.Msg.StringMsg
import sceptre.plumbing.{Route, Msg}

abstract class QuickStringProcessor(name: String) {

  case class SendHelper(send : Msg => Unit, sendBack: Msg => Unit) {
    def send(str: String): Unit = send(StringMsg(str))
  }

  def onMsg(str: String, sender: SendHelper)

  def apply(route: Route) = Processor.Builder(s"$name-${route.name}")(new Instance(route))

  class Instance(route: Route) extends RoutedProcessor(route) {
    val sendHelper = SendHelper(this.send, this.sendBack)

    def processMsg(msg: Msg): Unit = msg match {
      case StringMsg(str) => onMsg(str, sendHelper)
      case _ => send(msg)
    }
  }

}
