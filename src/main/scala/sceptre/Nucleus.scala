package sceptre

import akka.actor.Actor
import rx.lang.scala.Subscription
import sceptre.plumbing.{Msg, Terminus}


abstract class Nucleus(wireServerFacing: Terminus[Msg] => Subscription) {

  def newClientFacing(): Terminus[Msg]

//  class NucActor extends Actor {
//
//    var outboundSub: Option[Subscription] = None
//    override def receive: Receive = {
//
//  }
}
