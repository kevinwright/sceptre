package sceptre.plumbing

import akka.util.ByteString
import sceptre.protocol.TelnetCodes
import sceptre.protocol.TelnetCodes.Code

trait Msg {
  def toByteString: ByteString
  def toDebugString: String
}

trait InternalMsg extends Msg {
  val toByteString =  ByteString.empty
}

object Msg {

  case class FrameEnd(input: ByteString) extends InternalMsg {
    val toDebugString = "-FRAME-END-"
  }

  /**
   * Sent downstream to allow subsequent transforms to react to Msg-level negotiations
   * such as MXP, GMCP, NAWS, etc.
   */
  case class TelnetNegotiated(state: Code, feature: Code) extends InternalMsg {
    val toDebugString = s"-Negotiated-${state.id}-${feature.id}-"
  }

  case object TelnetEnableBinary extends InternalMsg {
    val toDebugString = s"-Telnet-Enable-Binary-"
  }

  case object TelnetEnableCompression extends InternalMsg {
    val toDebugString = s"-Telnet-Enable-Compression-"
  }

  trait Text extends Msg {
    def str: String
    def toByteString = ByteString(str)
    def debugPrefix: String
    def toDebugString = debugPrefix + " " + str.replace(27.toChar.toString, "§")
  }

  case class Line(str: String) extends Text { val debugPrefix = "Line: " }
  case class PartLine(str: String) extends Text { val debugPrefix = "PartLine: " }
  case class Prompt(str: String) extends Text { val debugPrefix = "Prompt: " }

  import sceptre.protocol.TelnetCodes._

  trait TelnetSeq extends Msg {
    def bytes: Seq[Byte]
    def toByteString = ByteString(bytes : _*)
    def toDebugString = CodeSeq.fromBytes(bytes).toString
  }

  case object TelnetGa extends TelnetSeq { val bytes = CodeSeq(IAC, GA).bytes }

  sealed abstract class TelnetNegotiate(val state: Code) extends TelnetSeq {
    def feature: Code
    lazy val bytes = TelnetCodes.CodeSeq(IAC, state, feature).bytes
    override def toDebugString = toString
  }
  case class TelnetWill(feature: Code) extends TelnetNegotiate(WILL)
  case class TelnetWont(feature: Code) extends TelnetNegotiate(WONT)
  case class TelnetDo(  feature: Code) extends TelnetNegotiate(DO)
  case class TelnetDont(feature: Code) extends TelnetNegotiate(DONT)

  object TelnetWill {def apply(b: Byte): TelnetWill = TelnetWill( TelnetCodes(b) )}
  object TelnetWont {def apply(b: Byte): TelnetWont = TelnetWont( TelnetCodes(b) )}
  object TelnetDo   {def apply(b: Byte): TelnetDo   = TelnetDo  ( TelnetCodes(b) )}
  object TelnetDont {def apply(b: Byte): TelnetDont = TelnetDont( TelnetCodes(b) )}

  case class TelnetSubnegotiate(feature: Code, subsequence: CodeSeq) extends TelnetSeq {
    val codeSeq = CodeSeq(IAC, SB, feature) ++ subsequence ++ CodeSeq(IAC, SE)
    val bytes = codeSeq.bytes
    lazy val featureStr = feature.id
    lazy val subseqStr = subsequence.bytes.map(_.toInt & 0xFF).mkString(",")
    override def toDebugString = s"IAC,SB,$featureStr,$subseqStr,IAC,SE"
  }


}

