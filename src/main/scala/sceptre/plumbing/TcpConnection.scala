package sceptre.plumbing

import akka.util.ByteString
import rx.lang.scala.{Observable, Observer}

trait TcpConnection {
  def output: Observer[ByteString]
  def input: Observable[ByteString]
}
