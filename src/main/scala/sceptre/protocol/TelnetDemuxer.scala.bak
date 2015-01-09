package sceptre.protocol

import akka.util.ByteString
import org.slf4j.LoggerFactory
import sceptre.plumbing.Msg._
import sceptre.plumbing.{Route, Envelope, Msg}
import sceptre.protocol.TelnetCodes._

import scala.collection.mutable.ListBuffer

/**
 * Converts a ByteString into an Envelope, containing defragmented lines and telnet protocol messages
 */
class TelnetDemuxer(route: Route) extends Function1[ByteString, Envelope] {

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

  def apply(byteStr: ByteString): Envelope = {

    val iter = byteStr.iterator

    val outBuffer = ListBuffer.empty[Msg]

    def flushText(): Unit = {
      if (textbuffer.length > 0) {
        val str = textbuffer.result().utf8String
        outBuffer += StringMsg(str)
        textbuffer.clear()
      }
    }

    def flushSubNeg(feature: Code): Unit = {
      outBuffer += TelnetSubnegotiate(feature, CodeSeq fromBytes subNegBuffer.result())
      subNegBuffer.clear()
    }

    import iter.{hasNext, next}

    while (hasNext) {
      state match {
        case InText => next() match {
          case IAC.value =>
            flushText()
            state = InProto
          case '\r' =>
          case '\n' => textbuffer += '\n'; flushText()
          case x => textbuffer += x
        }
        case InProto => next() match {
          case GA.value => outBuffer += TelnetGa
          case WILL.value => outBuffer += TelnetWill(next())
          case WONT.value => outBuffer += TelnetWont(next())
          case DO.value => outBuffer += TelnetDo(next())
          case DONT.value => outBuffer += TelnetDont(next())
          case SB.value => state = OpeningSubNeg
          case code =>
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

    Envelope.issue(outBuffer.toList, route)
  }

}
