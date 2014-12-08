package sceptre.processors

import sceptre.plumbing.Msg._
import sceptre.plumbing.{Envelope, Msg, Route}

object MessageLogger {
  def apply(typesToLog: Class[_]*) =
    Processor.Builder("MessageLogger")(new MessageLogger(typesToLog))
}

class MessageLogger(typesToLog: Seq[Class[_]]) extends Processor {
  def process: Process = {
    case env @ Envelope(msgs, route) =>
      msgs foreach { msg =>
        if (typesToLog.exists(_ isAssignableFrom msg.getClass))
          logMsg(msg, route)
      }
      passthru(env)
    case env => passthru(env)
  }

  def logMsg(msg: Msg, route: Route): Unit = msg match {
    case m : StringMsg =>
      val text = m.utf8String
      log.info(s"${route.name} - text: ${text.replace(27.toChar, '◙').lines.mkString("\\n\n")}")
    case m : TelnetSeqMsg =>
      val str = m.utf8String
      log.info(s"${route.name} - telnet: $str")
    case m : MxpEscape =>
      val str = m.utf8String
      log.info(s"${route.name} - mxp: $str")
    case m =>
      log.info(s"${route.name}: $m")
  }
}
