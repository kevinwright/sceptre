package sceptre.processors

import akka.actor._
import sceptre._
import sceptre.plumbing._

import scala.reflect.ClassTag

object Processor {
  case class Builder[T <: Processor: ClassTag](name: String)(creator: ⇒ T) {
    def build()(implicit factory: ActorRefFactory): ActorRef = {
      factory.actorOf(Props.apply(creator), name)
    }
    def withQualifier(qual: String): Builder[T] = {
      val newName = name + "-" + qual
      new Builder(newName)(creator)
    }
  }
}

abstract class Processor extends Actor with ActorLogging {
  val inbound = DeferredActorRef()
  val outbound = DeferredActorRef()

  override def receive: Receive = {
    case ConnectRoute(Inbound, ref) =>
      inbound set ref
      ref ! ConnectRoute(Outbound, self)
      log.debug(s" --> ${ref.path.name}")

    case ConnectRoute(Outbound, ref) =>
      outbound set ref
      log.debug(s" <-- ${ref.path.name}")

    case env: Envelope => process(env)
  }

  def send(env: Envelope): Unit = env match {
    case Envelope(_, Inbound) => inbound ! env
    case Envelope(_, Outbound) => outbound ! env
  }

  type Process = (Envelope) => Unit

  def process: Process
}


