package sceptre.plumbing

sealed class MxpTag(id: Int, val name: String) {
  val esc = 27.toChar
  def escapeSeq: String = s"$esc[${id}z"
}

class MxpLineTag(id: Int, name: String) extends MxpTag(id, name)

object MxpTag {
  case object OpenLine         extends MxpLineTag(0,  "Open")
  case object SecureLine       extends MxpLineTag(1,  "Secure")
  case object RawLine          extends MxpLineTag(2,  "Raw")

  //  case object Reset        extends MxpTag(3,  "MXP Reset")
  //  case object TempSecure   extends MxpTag(4,  "MXP Temp Secure")
  //  case object LockOpen     extends MxpTag(5,  "MXP Lock Open")
  //  case object LockSecure   extends MxpTag(6,  "MXP Lock Secure")
  //  case object LockRaw      extends MxpTag(7,  "MXP Lock Secure")

  case object RoomNameLine     extends MxpLineTag(10, "Room Name")
  case object RoomDescLine     extends MxpLineTag(11, "Room Desc")
  case object RoomExitsLine    extends MxpLineTag(12, "Room Exits")
  case object WelcomeTextLine  extends MxpLineTag(19, "Welcome Text")
}