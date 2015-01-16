package sceptre.protocol

import org.slf4j.LoggerFactory
import rx.lang.scala.Observable
import sceptre.plumbing._
import sceptre.plumbing.Msg._
import sceptre.protocol.TelnetCapability._
import sceptre.protocol.TelnetCodes._

class TelnetNegotiator(name: String, capabilities: TelnetCapability*) extends Function1[Observable[Msg], Observable[Response[Msg]]] {
  private val log = LoggerFactory.getLogger(this.getClass)

  def supports(feature: Code) = capabilities contains Support(feature)
  def rejects(feature: Code) = capabilities contains Reject(feature)
  def provides(feature: Code) = capabilities contains Provide(feature)
  def refuses(feature: Code) = capabilities contains Refuse(feature)
  def donthandle(feature: Code) = capabilities contains Passthrough(feature)

  def passthroughAllOthers = capabilities contains PassthroughAllOthers

  def apply(input: Observable[Msg]): Observable[Response[Msg]] = {

    log.info(s"$name telnet negotiator wired")
    var sent: Set[TelnetCapability] = Set.empty
    var negotiated: Set[(Code, Code)] = Set.empty

    def solicited(feature: Code) = sent contains Solicit(feature)
    def proffered(feature: Code) = sent contains Proffer(feature)

    val openingSalvo: Seq[Response[Msg]] = capabilities.collect{
      case cap@Proffer(feature) =>
        sent += cap
        Reply(TelnetWill(feature))
      case cap@Solicit(feature) =>
        sent += cap
        Reply(TelnetDo(feature))
    }

      /**
       * Builds the output "notification" message to inform downstream stages
       * that a telnet protocol has been negotiated.
       *
       * Also calculates any "reply" message to the originating telnet channel.
       * Used only on negotiating specific features that affect the
       * ByteString/Msg boundary such as TRANSMIT_BINARY and MCCP compression.
       */
    def notificationsFor(state: Code, feature: Code): List[Response[Msg]] = {
        Output(TelnetNegotiated(state, feature)) :: (
          (state, feature) match {
            case (WILL, TRANSMIT_BINARY) => List(Reply(TelnetEnableBinary))
            case (WILL, MCCP) => List(Reply(TelnetEnableCompression))
            case _ => List.empty
          }
        )
      }

    // state/feature are the codes we're accepting with
    def accept(state: Code, feature: Code): Observable[Response[Msg]] = {
      state match {
        case DO | DONT => sent -= Solicit(feature)
        case WILL | WONT => sent -= Proffer(feature)
      }
      log.info(s"$name negotiated: ${state.id} ${feature.id}")
      negotiated += (state -> feature)
      val negotiation = TelnetNegotiated(state, feature)
      Observable.just(notificationsFor(state, feature): _*)
    }

    def reply(neg: TelnetNegotiate): Observable[Response[Msg]] = {
      negotiated += (neg.state -> neg.feature)
      log.info(s"$name negotiated: ${neg.state.id} ${neg.feature.id}")
      val msgs = Reply(neg) :: notificationsFor(neg.state, neg.feature)
      Observable.just(msgs: _*)
    }

    def send(msg: Msg): Observable[Response[Msg]] = Observable just Output(msg)

    def processMsg(msg: Msg): Observable[Response[Msg]] = msg match {
      case x: TelnetNegotiate if donthandle(x.feature) =>
        log.info(s"$name passing through telnet message ${msg.toDebugString}")
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
        log.info(s"$name unexpected telnet message ${msg.toDebugString}, passing through")
        send(msg)

      case _ => send(msg)
    }

    Observable.just(openingSalvo: _*) ++ (input concatMap processMsg)
  }
}

object TelnetNegotiator {
  def apply (name: String, capabilities: TelnetCapability*) = new TelnetNegotiator(name, capabilities: _*)
}
