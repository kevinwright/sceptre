package sceptre.protocol

import akka.util.ByteString
import org.slf4j.LoggerFactory
import rx.lang.scala.Observable
import sceptre.plumbing.Msg
import sceptre.protocol.TelnetCodes._

import scala.collection.mutable.ListBuffer

class TelnetMuxer extends Function1[Msg, Observable[ByteString]] {
  var buffer: ByteString = ByteString.empty

  def apply(msg: Msg): Observable[ByteString] = msg match {
    case Msg.FrameEnd(srcBytes) if buffer.isEmpty =>
      Observable.empty
    case Msg.FrameEnd(srcBytes) =>
      val ret = Observable.just(buffer)
      buffer = ByteString.empty
      ret
    case m =>
      buffer = buffer ++ m.toByteString
      Observable.empty
  }
}

/**
 * Converts a ByteString into an Envelope, containing defragmented lines and telnet protocol messages
 */
class TelnetDemuxer() extends Function1[ByteString, Observable[Msg]] {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait State
  case object InText extends State
  case object InProto extends State
  case object OpeningSubNeg extends State
  case class  InSubNeg(feature: Code) extends State
  case class  InClosableSubNeg(feature: Code) extends State

  private[this] var state: State = InText
  private[this] val textbuffer = ByteString.newBuilder
  private[this] val subNegBuffer = ByteString.newBuilder

  def apply(byteStr: ByteString): Observable[Msg] = {

    val iter = byteStr.iterator

    val outBuffer = ListBuffer.empty[Msg]

    def flushLine(): Unit = {
      if (textbuffer.length > 0) {
        val str = textbuffer.result().utf8String
        outBuffer += Msg.Line(str)
        textbuffer.clear()
      }
    }

    def flushPartLine(): Unit = {
      if (textbuffer.length > 0) {
        val str = textbuffer.result().utf8String
        outBuffer += Msg.PartLine(str)
        textbuffer.clear()
      }
    }

    def flushPrompt(): Unit = {
      if (textbuffer.length > 0) {
        val str = textbuffer.result().utf8String
        outBuffer += Msg.Prompt(str)
        textbuffer.clear()
      }
    }

    def flushSubNeg(feature: Code): Unit = {
      outBuffer += Msg.TelnetSubnegotiate(feature, CodeSeq fromBytes subNegBuffer.result())
      subNegBuffer.clear()
    }

    import iter.{hasNext, next}

    while (hasNext) {
      state match {
        case InText => next() match {
          case IAC.value =>
            state = InProto
          //case '\r' =>
          case '\n' =>
            textbuffer += '\n'
            flushLine()
          case x => textbuffer += x
        }
        case InProto => next() match {
          case GA.value   => flushPrompt(); outBuffer += Msg.TelnetGa; state = InText
          case WILL.value => flushPartLine(); outBuffer += Msg.TelnetWill(next()); state = InText
          case WONT.value => flushPartLine(); outBuffer += Msg.TelnetWont(next()); state = InText
          case DO.value   => flushPartLine(); outBuffer += Msg.TelnetDo(next());   state = InText
          case DONT.value => flushPartLine(); outBuffer += Msg.TelnetDont(next()); state = InText
          case SB.value =>
            flushPartLine()
            state = OpeningSubNeg
          case code =>
            flushPartLine()
            log.error("invalid telnet sequence: " + CodeSeq(IAC, TelnetCodes(code)).toString)
            state = InText
        }
        case OpeningSubNeg => next() match {
          case x => state = InSubNeg(TelnetCodes(x))
        }
        case InSubNeg(feature) => next() match {
          case IAC.value => state = InClosableSubNeg(feature)
          case x => subNegBuffer += x
        }
        case InClosableSubNeg(feature) => next() match {
          case SE.value =>
            flushSubNeg(feature)
            state = InText
          case x =>
            subNegBuffer += x
            state = InSubNeg(feature)
        }
      }
    }

    require(
      textbuffer.length == 0 || state == InText,
      "Not in text state, text buffer should be empty"
    )
    require(
      subNegBuffer.length == 0 || state.isInstanceOf[InSubNeg] || state.isInstanceOf[InClosableSubNeg],
      "Not in subnegotiation state, subnegotiation buffer should be empty"
    )

    flushPartLine()

    outBuffer += Msg.FrameEnd(byteStr)

    Observable from outBuffer
  }

}
