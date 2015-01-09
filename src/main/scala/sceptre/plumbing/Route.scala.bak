package sceptre.plumbing

import akka.actor.ActorRef

sealed trait Route { def name: String; def reverse: Route }
case object NoRoute extends Route { val name = "none"; val reverse = NoRoute }
case object Inbound extends Route { val name = "inbound"; val reverse = Outbound }
case object Outbound extends Route { val name = "outbound"; val reverse = Inbound }

case class ConnectRoute(route: Route, actorRef: ActorRef)


