package sceptre.plumbing

import rx.lang.scala.{Subscription, Observable}

sealed trait Response[+T] { def content: T }
case class Output[+T](content: T) extends Response[T]
case class Reply[+T](content: T) extends Response[T]

case class AdaptedTerminus[T](
  source  : Observable[T],
  connect : () => Subscription,
  sink    : (Observable[T]) => Subscription
) extends Terminus[T]

trait Terminus[T]  {
  def source: Observable[T]
  def sink: (Observable[T]) => Subscription
  def connect: () => Subscription

  def >>[P,R](p: P)(implicit pipable: P => Pipable.Aux[T,R]): R = pipable(p).pipeFrom(this)
}

