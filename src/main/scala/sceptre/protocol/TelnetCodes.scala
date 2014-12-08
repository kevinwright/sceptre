package sceptre.protocol

object TelnetCodes {

  def apply(b: Byte): Code = Code lookup b

  private[this] var _all = Set.empty[Code]
  def all: Set[Code] = _all

  //only codes build from an int get auto-registered
  class Code(val value: Byte, val id: String, val description: String)

  private object Code{
    def apply(value: Int, id: String, description: String = "") = {
      val code = new Code(value.toByte, id, if (description == "") id else description)
      _all += code
      code
    }

    def lookup(b: Byte): Code = {
      all.find(_.value == b) match {
        case Some(code) => code
        case None =>
          val adhoc = Code(b, b.toString)
          adhoc
      }

    }
  }

  case class CodeSeq(seq: Code*) {
    override def toString = seq map {_.id} mkString ","
    def bytes = seq map {_.value}
    def ++(other: CodeSeq): CodeSeq = {
      val newSeq = this.seq ++ other.seq
      new CodeSeq(newSeq : _*)
    }
  }
  object CodeSeq {
    def fromBytes(bytes: Seq[Byte]): CodeSeq = CodeSeq(bytes map Code.lookup : _*)
  }



  val IAC  = Code(255, "IAC", "Interpret as command")

  val GA   = Code(249, "GA", "Go-Ahead")

  val WILL = Code(251, "WILL")
  val WONT = Code(252, "WONT")
  val DO   = Code(253, "DO")
  val DONT = Code(254, "DONT")

  val SB   = Code(250, "SB" , "Subnegotiation start")
  val SE   = Code(240, "SE" , "End of subnegotiation parameters")

  val NOP  = Code(241, "NOP", "No operation")
  val DM   = Code(242, "DM" , "Data Mark")
  val BRK  = Code(243, "BRK", "NVT Break")
  val IP   = Code(244, "IP" , "Interrupt Process")
  val AO   = Code(245, "AO" , "Abort output")
  val AYT  = Code(246, "AYT", "Are You There")
  val EC   = Code(247, "EC" , "Erase character")
  val EL   = Code(248, "EL" , "Erase Line")

//  object WWDD {
//    def unapply(b: Byte) = b match {
//      case WILL | WONT | DO | DONT => Some(b)
//      case _ => None
//    }
//  }

  // Standard Negotiable Options
  val TRANSMIT_BINARY     = Code( 0, "TRANSMIT_BINARY", "Binary Transmission")
  val ECHO                = Code( 1, "ECHO")
  val SUPPRESS_GO_AHEAD   = Code( 3, "SUPPRESS_GO_AHEAD")
  val STATUS              = Code( 5, "STATUS")
  val TIMING_MARK         = Code( 6, "TIMING_MARK")
  val NAOCRD              = Code(10, "NAOCRD", "Output Carriage-Return Disposition")
  val NAOHTS              = Code(11, "NAOHTS", "Output Horizontal Tab Stops")
  val NAOHTD              = Code(12, "NAOHTD", "Output Horizontal Tab Stop Disposition")
  val NAOFFD              = Code(13, "NAOFFD", "Output Formfeed Disposition")
  val NAOVTS              = Code(14, "NAOVTS", "Output Vertical Tabstops")
  val NAOVTD              = Code(15, "NAOVTD", "Output Vertical Tab Disposition")
  val NAOLFD              = Code(16, "NAOLFD", "Output Linefeed Disposition")
  val EXTEND_ASCII        = Code(17, "EXTEND_ASCII")
  val TERMINAL_TYPE       = Code(24, "TERMINAL_TYPE")
  val NAWS                = Code(31, "NAWS", "Negotiate About Window Size")
  val TERMINAL_SPEED      = Code(32, "TERMINAL_SPEED")
  val TOGGLE_FLOW_CONTROL = Code(33, "TOGGLE_FLOW_CONTROL")
  val LINEMODE            = Code(34, "LINEMODE")
  val X_DISPLAY           = Code(35, "X_DISPLAY")
  val ENVVARS             = Code(36, "ENVVARS")
  val AUTHENTICATION      = Code(37, "AUTHENTICATION")
  val ENCRYPTION          = Code(38, "ENCRYPTION")
  val NEW_ENV             = Code(39, "NEW_ENV")

  val MSDP                = Code(69,  "MSDP", "Mud Server Data Protocol")
  val MSSP                = Code(70,  "MSSP", "Mud Server Status Protocol")
  val MXP                 = Code(91,  "MXP", "Mud Extension Protocol")
  val GMCP                = Code(201, "GMCP")

  // Not features as such, used inside subnegotiations...

  //  val MSSP_VAR            = Code( 1, "MSSP_VAR")
  //  val MSSP_VAL            = Code( 2, "MSSP_VAL")

  //  val MSDP_VAR            = Code( 1, "MSDP_VAR")
  //  val MSDP_VAL            = Code( 2, "MSDP_VAL")
  //  val MSDP_TABLE_OPEN     = Code( 3, "MSDP_TABLE_OPEN ")
  //  val MSDP_TABLE_CLOSE    = Code( 4, "MSDP_TABLE_CLOSE")
  //  val MSDP_ARRAY_OPEN     = Code( 5, "MSDP_ARRAY_OPEN ")
  //  val MSDP_ARRAY_CLOSE    = Code( 6, "MSDP_ARRAY_CLOSE")


}

