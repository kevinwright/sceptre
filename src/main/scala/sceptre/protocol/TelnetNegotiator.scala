package sceptre.protocol

import org.slf4j.LoggerFactory
import rx.lang.scala.Observable
import sceptre.plumbing._
import sceptre.plumbing.Msg._
import sceptre.protocol.TelnetCapability._
import sceptre.protocol.TelnetCodes._


case class TelnetNegotiator(capabilities: TelnetCapability*) {

  private val log = LoggerFactory.getLogger(this.getClass)

  def supports(feature: Code) = capabilities contains Support(feature)
  def rejects(feature: Code) = capabilities contains Reject(feature)
  def provides(feature: Code) = capabilities contains Provide(feature)
  def refuses(feature: Code) = capabilities contains Provide(feature)
  def donthandle(feature: Code) = capabilities contains Passthrough(feature)

  def passthroughAllOthers = capabilities contains PassthroughAllOthers



  val wire: (Observable[Msg]) => Observable[Response[Msg]] = (input) => {

    log.info("telnet negotiator wired")
    var sent: Set[TelnetCapability] = Set.empty
    var negotiated: Set[(Code, Code)] = Set.empty

    def solicited(feature: Code) = sent contains Solicit(feature)
    def proffered(feature: Code) = sent contains Proffer(feature)

    val openingSalvo: Observable[Response[Msg]] = Observable just {
      capabilities.collect{
        case cap@Proffer(feature) => sent += cap; Reply(TelnetWill(feature))
        case cap@Solicit(feature) => sent += cap; Reply(TelnetDo(feature))
      }: _*
    }

    def accept(state: Code, feature: Code): Observable[Response[Msg]] = {
      state match {
        case DO | DONT => sent -= Solicit(feature)
        case WILL | WONT => sent -= Proffer(feature)
      }
      log.info(s"Negotiated: ${state.id} ${feature.id}")
      negotiated += (state -> feature)
      Observable.empty
    }

    def reply(msg: Msg): Observable[Response[Msg]] = msg match {
      case neg: TelnetNegotiate =>
        negotiated += (neg.state -> neg.feature)
        log.info(s"Negotiated: ${neg.state.id} ${neg.feature.id}")
        Observable just Reply(neg)
      case m => Observable just Reply(m)
    }

    def send(msg: Msg): Observable[Response[Msg]] = Observable just Output(msg)

    def processMsg(msg: Msg): Observable[Response[Msg]] = msg match {
      case x: TelnetNegotiate if donthandle(x.feature) =>
        log.info(s"passing through telnet message ${msg.toDebugString}")
        send(msg)

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

      case msg: TelnetNegotiate =>
        log.info(s"Unexpected telnet message ${msg.toDebugString}, passing through")
        send(msg)

      case _ => send(msg)
    }

    openingSalvo ++ (input concatMap processMsg)
  }







}
