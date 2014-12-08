package sceptre.plumbing

import akka.util.ByteString
import sceptre.protocol.TelnetCodes

trait Msg {
  def byteString: ByteString
  def utf8String: String
}
object Msg {
  case class ByteStringMsg(bs: ByteString) extends Msg {
    def byteString = bs
    def utf8String = bs.utf8String
  }
  case class StringMsg(str: String) extends Msg {
    def byteString = ByteString(str)
    def utf8String = str
  }

  import sceptre.protocol.TelnetCodes._

  trait TelnetSeqMsg extends Msg {
    def bytes: Seq[Byte]
    def byteString = ByteString(bytes: _*)
    def utf8String = CodeSeq.fromBytes(bytes).toString
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
    override def utf8String = s"IAC,SB,$featureStr,$subseqStr,IAC,SE"
  }

  class MxpEscape(id: Int, val utf8String: String) extends Msg {
    val esc = 27.toChar
    def byteString: ByteString = ByteString(s"$esc[${id}z")
  }
  case object MxpOpenLine     extends MxpEscape(0,  "MXP Open Line")
  case object MxpSecureLine   extends MxpEscape(1,  "MXP Secure Line")
  case object MxpRawLine      extends MxpEscape(2,  "MXP Raw Line")
  case object MxpReset        extends MxpEscape(3,  "MXP Reset")
  case object MxpTempSecure   extends MxpEscape(4,  "MXP Temp Secure")
  case object MxpLockOpen     extends MxpEscape(5,  "MXP Lock Open")
  case object MxpLockSecure   extends MxpEscape(6,  "MXP Lock Secure")
  case object MxpLockRaw      extends MxpEscape(7,  "MXP Lock Secure")

  case object MxpRoomName     extends MxpEscape(10, "MXP Room Name Line")
  case object MxpRoomDesc     extends MxpEscape(11, "MXP Room Description Line")
  case object MxpRoomExits    extends MxpEscape(12, "MXP Room Exits Line")
  case object MxpWelcomeText  extends MxpEscape(19, "MXP Welcome Text Line")

}

