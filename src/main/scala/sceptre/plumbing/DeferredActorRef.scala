package sceptre.plumbing

import akka.actor._

import scala.collection.mutable

case class DeferredActorRef(implicit factory: ActorRefFactory) {
  private[this] val innerActor = factory.actorOf(Props(new TheActor))

  private[this] case class SetRef(ref: ActorRef)

  private[this] class TheActor extends Actor with ActorLogging {
    val buffer: mutable.Queue[(Any, ActorRef)] = mutable.Queue.empty

    override def receive: Receive = {
      case SetRef(ref) =>
        buffer foreach { case (msg, snd) =>
//          log.info(s"de-queuing to ${ref.path.name}: $msg")
          ref.!(msg)(snd)
        }
        buffer.clear()
        context become receive2(ref)

      case msg =>
//        log.info(s"queuing: $msg")
        buffer enqueue (msg -> context.sender())
    }

    def receive2(ref: ActorRef): Receive = {
      case msg =>
//        log.info(s"sending direct to ${ref.path.name}: $msg")
        ref forward msg
    }
  }

  def !(msg: Any)(implicit ctx: ActorContext) = innerActor ! msg
  def forward(msg: Any)(implicit ctx: ActorContext) = innerActor forward msg
  def set(ref: ActorRef) = innerActor ! SetRef(ref)
}
