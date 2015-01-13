package sceptre.protocol

import rx.lang.scala.Observable
import sceptre.plumbing.Msg._
import sceptre.protocol.TelnetCapability._
import sceptre.protocol.TelnetCodes._


class TelnetNegotiator(capabilities: TelnetCapability*) extends Function1[TelnetNegotiate, (Observable[TelnetNegotiate], Observable[TelnetNegotiate])] {
  private[this] var sent: Set[TelnetCapability] = Set.empty
  private[this] var negotiated: Set[(Code, Code)] = Set.empty

  def supports(feature: Code) = capabilities contains Support(feature)
  def rejects(feature: Code) = capabilities contains Reject(feature)
  def provides(feature: Code) = capabilities contains Provide(feature)
  def refuses(feature: Code) = capabilities contains Provide(feature)
  def donthandle(feature: Code) = capabilities contains Passthrough(feature)
  def solicited(feature: Code) = sent contains Solicit(feature)
  def proffered(feature: Code) = sent contains Proffer(feature)

  def passthroughAllOthers = capabilities contains PassthroughAllOthers

  override def preStart: Unit = capabilities collect {
    case cap@Proffer(feature) => sent += cap; sendBack(TelnetWill(feature))
    case cap@Solicit(feature) => sent += cap; sendBack(TelnetDo(feature))
  }

  private[this] def accept(state: Code, feature: Code): Unit = {
    state match {
      case DO | DONT => sent -= Solicit(feature)
      case WILL | WONT => sent -= Proffer(feature)
    }
    log.info(s"Negotiated: ${state.id} ${feature.id}")
    negotiated += (state -> feature)
  }

  private[this] def reply(msg: Msg) = msg match {
    case neg: TelnetNegotiateMsg =>
      negotiated += (neg.state -> neg.feature)
      log.info(s"Negotiated: ${neg.state.id} ${neg.feature.id}")
      sendBack(neg)
    case m => sendBack(m)
  }

  def processMsg(msg: Msg): Unit = msg match {
    case x: TelnetNegotiateMsg if donthandle(x.feature) => send(msg)

    case TelnetWill(feature) if solicited(feature) => accept(DO, feature)
    case TelnetWont(feature) if solicited(feature) => accept(DONT, feature)
    case TelnetDo(feature)   if proffered(feature) => accept(WILL, feature)
    case TelnetDont(feature) if proffered(feature) => accept(WONT, feature)

    case TelnetWill(feature) if supports(feature)  => reply(TelnetDo(feature))
    case TelnetWill(feature) if rejects(feature)   => reply(TelnetDont(feature))
    case TelnetWont(feature) if rejects(feature)   => reply(TelnetDont(feature))
    case TelnetDo(feature)   if provides(feature)  => reply(TelnetWill(feature))
    case TelnetDo(feature)   if refuses(feature)   => reply(TelnetWont(feature))
    case TelnetDont(feature) if refuses(feature)   => reply(TelnetWont(feature))

    case msg: TelnetNegotiateMsg =>
      log.info(s"Unexpected telnet message ${msg.utf8String}, passing through")
      send(msg)

    case _ => send(msg)
  }
}
