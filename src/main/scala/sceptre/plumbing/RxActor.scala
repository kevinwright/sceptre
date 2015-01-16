package sceptre.plumbing

import akka.actor.{ActorLogging, Actor, Props}
import rx.lang.scala.{Notification, Subscriber}

import scala.reflect.ClassTag

object RxActor {
  def wrapping[T: ClassTag](subscriber: Subscriber[T]) = Props(new RxActor(subscriber))
}

class RxActor[T: ClassTag](subscriber: Subscriber[T]) extends Actor with ActorLogging {
  def receive: Receive = {
    case Notification.OnNext(value: T) => subscriber.onNext(value)
    case Notification.OnCompleted => subscriber.onCompleted()
    case Notification.OnError(value) => subscriber.onError(value)
  }
}
