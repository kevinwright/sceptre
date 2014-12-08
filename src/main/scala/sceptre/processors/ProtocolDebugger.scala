package sceptre.processors

import sceptre.plumbing.Msg.TelnetSeqMsg
import sceptre.plumbing.{Envelope, Inbound, Msg}

object ProtocolDebugger {
  def apply(typesToDebug: Class[_]*) =
    new Processor.Builder("ProtocolDebugger")(new ProtocolDebugger(typesToDebug))
}

class ProtocolDebugger(typesToDebug: Seq[Class[_]]) extends RoutedProcessor(Inbound) {
  override def process: Process = {
    case env @ Envelope(msgs, Inbound)  =>
      msgs foreach { msg =>
        if(typesToDebug.exists(_ isAssignableFrom msg.getClass))
          processMsg(msg)
      }
      passthru(env)
    case env => passthru(env)
  }

  def processMsg(msg: Msg): Unit = msg match {
    case m : TelnetSeqMsg =>
      val str = m.utf8String
      send(s"<<$str>>")
  }
}
