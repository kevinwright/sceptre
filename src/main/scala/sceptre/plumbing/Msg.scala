package sceptre.plumbing

import sceptre.protocol.TelnetCodes

trait Msg {
  def toDebugString: String
}

object Msg {

  case class Line(str: String, mxpTag: Option[MxpLineTag]) extends Msg {
    def toDebugString = (mxpTag map (tag => s"[${tag.name}]$str") getOrElse str).replace(27.toChar.toString, "§")
  }

  import sceptre.protocol.TelnetCodes._

  trait TelnetSeqMsg extends Msg {
    def bytes: Seq[Byte]
    def toDebugString = CodeSeq.fromBytes(bytes).toString
  }

  case object TelnetGa extends TelnetSeqMsg { val bytes = CodeSeq(IAC, GA).bytes }

  abstract class TelnetNegotiateMsg(val state: Code) extends TelnetSeqMsg {
    def feature: Code
    lazy val bytes = TelnetCodes.CodeSeq(IAC, state, feature).bytes
  }
  case class TelnetWill(feature: Code) extends TelnetNegotiateMsg(WILL)
  case class TelnetWont(feature: Code) extends TelnetNegotiateMsg(WONT)
  case class TelnetDo(  feature: Code) extends TelnetNegotiateMsg(DO)
  case class TelnetDont(feature: Code) extends TelnetNegotiateMsg(DONT)

  object TelnetWill {def apply(b: Byte): TelnetWill = TelnetWill( TelnetCodes(b) )}
  object TelnetWont {def apply(b: Byte): TelnetWont = TelnetWont( TelnetCodes(b) )}
  object TelnetDo   {def apply(b: Byte): TelnetDo   = TelnetDo  ( TelnetCodes(b) )}
  object TelnetDont {def apply(b: Byte): TelnetDont = TelnetDont( TelnetCodes(b) )}

  case class TelnetSubnegotiate(feature: Code, subsequence: CodeSeq) extends TelnetSeqMsg {
    val codeSeq = CodeSeq(IAC, SB, feature) ++ subsequence ++ CodeSeq(IAC, SE)
    val bytes = codeSeq.bytes
    lazy val featureStr = feature.id
    lazy val subseqStr = subsequence.toString
    override def toDebugString = s"IAC,SB,$featureStr,$subseqStr,IAC,SE"
  }


}

