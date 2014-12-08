package sceptre.plumbing

import org.joda.time.Duration

case class Envelope private(msgs: Seq[Msg], route: Route)(created: Long) {
  def age(): Duration = {
    val now = System.nanoTime()
    val ageNanos = now - created
    Duration.millis(ageNanos / 1000L)
  }
}

object Envelope {
  def issue(msgs: Seq[Msg], route: Route) =
    new Envelope(msgs, route)(System.nanoTime())
}