package sceptre.protocol

import akka.util.ByteString
import rx.lang.scala.Observable
import sceptre.plumbing.Msg.{TelnetEnableBinary, Text}
import sceptre.plumbing.{InternalMsg, Msg}

class TelnetMuxer extends Function1[Msg, Observable[ByteString]] {
  var buffer: ByteString = ByteString.empty

  val Twofivefive = 255.toByte

  var binaryMode: Boolean = false
  val bufferFrames = false

  def send(bs: ByteString): Observable[ByteString] = {
    buffer = buffer ++ bs
    Observable.empty
  }

  def apply(msg: Msg): Observable[ByteString] = msg match {
    case TelnetEnableBinary =>
      binaryMode = true
      Observable.empty

    case Msg.FrameEnd(srcBytes) if buffer.nonEmpty && bufferFrames =>
      val ret = Observable just buffer
      buffer = ByteString.empty
      ret

    case msg: InternalMsg =>
      Observable.empty // skip all other internal messages

    case m: Text if binaryMode =>
      send(m.toByteString flatMap {
        case Twofivefive => List(Twofivefive, Twofivefive)
        case x => List(x)
      })

    case m =>
      send(m.toByteString)
  }
}
