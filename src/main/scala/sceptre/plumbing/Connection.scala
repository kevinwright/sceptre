package sceptre.plumbing

import rx.lang.scala.{Subscription, Observable}

trait Connection[T] {
  def sink(source: Observable[T]): Subscription
  def source: Observable[T]
  def connect(): Unit
}