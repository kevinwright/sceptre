package sceptre.processors

import akka.util.ByteString
import org.slf4j.LoggerFactory
import sceptre.plumbing.{Reply, Output, Response, Msg}


case class MessageLogger(name: String) extends Function1[Response[Msg], Msg] {
  private val log = LoggerFactory.getLogger(this.getClass)

  def apply(resp: Response[Msg]): Msg = resp match {
    case Output(msg) => log.info(">>> " + name + ": " + msg.toDebugString); msg
    case Reply(msg) => log.info("<<< " + name + ": " + msg.toDebugString); msg
  }

}

case class BytesLogger(name: String) extends Function1[Response[ByteString], ByteString] {
  private val log = LoggerFactory.getLogger(this.getClass)

  def str(bytestr: ByteString): String = bytestr.iterator.map(_.toInt & 0xFF).mkString("[", ",", "]")

  def apply(resp: Response[ByteString]): ByteString = resp match {
    case Output(bytestr) => log.info(">>> " + name + ": " + str(bytestr)); bytestr
    case Reply(bytestr) => log.info("<<< " + name + ": " + str(bytestr)); bytestr
  }

}
