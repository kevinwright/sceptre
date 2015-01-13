package sceptre.protocol

import sceptre.protocol.TelnetCodes.Code

sealed trait TelnetCapability { def feature: Code }
object TelnetCapability {
  /** A capability we *want* to provide, and pro-actively offer if required **/
  case class Proffer(feature: Code) extends TelnetCapability
  /** A capability we can handle, if the remote asks it of us **/
  case class Provide(feature: Code) extends TelnetCapability
  /** A capability we want the server to provide, and pro-actively request if necessary **/
  case class Solicit(feature: Code) extends TelnetCapability
  /** A capability we can work with, if the server offers it **/
  case class Support(feature: Code) extends TelnetCapability
  /** A capability we reject if the server offers it **/
  case class Reject(feature: Code) extends TelnetCapability
  /** A capability we refuse if the server requests it **/
  case class Refuse(feature: Code) extends TelnetCapability
  /** A capability we pass through unaltered **/
  case class Passthrough(feature: Code) extends TelnetCapability
  /** A capability we pass through unaltered **/
  case object PassthroughAllOthers extends TelnetCapability { val feature = ??? }
}
