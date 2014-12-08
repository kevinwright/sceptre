package sceptre.plumbing

import akka.actor.ActorRef

sealed trait Route { def name: String }
case object Inbound extends Route { val name = "inbound" }
case object Outbound extends Route { val name = "outbound" }

case class ConnectRoute(route: Route, actorRef: ActorRef)


