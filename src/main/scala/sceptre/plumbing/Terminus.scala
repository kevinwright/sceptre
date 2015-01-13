package sceptre.plumbing

import rx.lang.scala.{Subscription, Observable}

case class AdaptedTerminus[T](
  source  : Observable[T],
  connect : () => Unit,
  sink    : (Observable[T]) => Subscription
) extends Terminus[T]

trait Terminus[T]  {
  def source: Observable[T]
  def sink: (Observable[T]) => Subscription
  def connect: () => Unit

  /** wire through to a final terminus **/
  def >>(that: Terminus[T]): Subscription = {
    val sub = that sink this.source
    connect()
    sub
  }

  /** wire through as a final terminus **/
  def <<(that: Terminus[T]): Subscription = {
    val sub = this sink that.source
    connect()
    sub
  }

  /** transform source preserving message type **/
  def >>[U](fn: T => Observable[T]): Terminus[T] = AdaptedTerminus(
    source = this.source concatMap fn,
    sink = this.sink,
    connect = this.connect
  )

  /** transform sink preserving message type **/
  def <<[U](fn: T => Observable[T]): Terminus[T] = AdaptedTerminus(
    source = this.source,
    sink = (obs: Observable[T]) => this.sink(obs concatMap fn),
    connect = this.connect
  )
}

