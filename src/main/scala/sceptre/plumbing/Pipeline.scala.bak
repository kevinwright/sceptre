package sceptre.plumbing

import java.net.InetSocketAddress

import akka.actor._
import sceptre._
import sceptre.processors.Processor

class Pipeline(
  name            : String,
  clientAddr      : InetSocketAddress,
  serverAddr      : InetSocketAddress,
  processBuilders : Seq[Processor.Builder[_]]
)(implicit system: ActorSystem) {

  println(s"Starting Pipeline $name")
  val clientside = new ClientSideEndpoint

  class ClientSideEndpoint extends Endpoint("clientside", LocalFacing, clientAddr) {
    override def onConnectionEstablished(clientHandler: ActorRef): Unit = {
      new ServerSideEndpoint(clientHandler)
    }
  }

  class ServerSideEndpoint(clientHandler: ActorRef) extends Endpoint("serverside", RemoteFacing, serverAddr) {
    override def onConnectionEstablished(serverHandler: ActorRef): Unit = {
      wire(serverHandler, clientHandler)
    }
  }

  def wire(serverside: ActorRef, clientside: ActorRef): Unit = {
    val processors = processBuilders map { _.build() }
    val pipeEnd = processors.foldLeft(serverside)( (prev, curr) => {
//      println(s"CONNECTING $name: ${prev.path.name} --> ${curr.path.name}")
      prev ! ConnectRoute(Inbound, curr)
      curr
    } )
//    println(s"CONNECTING: $name ${pipeEnd.path.name} --> ${sink.path.name}")

    pipeEnd ! ConnectRoute(Inbound, clientside)
  }

}
