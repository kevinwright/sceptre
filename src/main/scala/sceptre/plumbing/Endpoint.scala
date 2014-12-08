package sceptre.plumbing

import java.net.InetSocketAddress

import akka.actor._
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import sceptre.protocol.TelnetDemuxer

sealed trait Intent
case object RemoteFacing extends Intent
case object LocalFacing extends Intent

abstract class Endpoint(name: String, intent: Intent, addr: InetSocketAddress)(implicit system: ActorSystem) {

  def onConnectionEstablished(handler: ActorRef): Unit

  //auto-start the connector on initialisations
  val connector = system.actorOf( Props(new Connector), s"$name-connector" )

  val route = intent match {
    case LocalFacing  => Outbound
    case RemoteFacing => Inbound
  }

  class Connector extends Actor with ActorLogging {
    intent match {
      case LocalFacing =>
        log.info(s"awaiting connections on $addr")
        IO(Tcp) ! Bind(self, addr)
      case RemoteFacing =>
        log.info(s"connecting to $addr")
        IO(Tcp) ! Connect(addr)
    }


    def receive: Receive = {
      case Connected(remote, _) =>
        log.info("address {} connected", remote)
        val sdr = sender()
        log.info(s"connected sender = $sdr")
        val handler = context.actorOf( Props( new Handler(remote, route, sdr) ), s"$name-handler" )
        sdr ! Register(handler)
      case CommandFailed(b: Bind) => log.error("TCP bind failed!")
      case other => log.warning(s"$other")
    }
  }

  class Handler (remote: InetSocketAddress, route: Route, connection: ActorRef) extends Actor with ActorLogging {

    val demux = new TelnetDemuxer(route)

    // We need to know when the connection dies without sending a `Tcp.ConnectionClosed`
    context.watch(connection)

    log.info(s"connection = $connection")
    onConnectionEstablished(self)

    val downstream = DeferredActorRef()(context)

    def writeOut(msg: Msg): Unit =  connection ! Tcp.Write(msg.byteString)

    def receive: Receive = {
      case ConnectRoute(Inbound, ref) =>
        downstream set ref
        ref ! ConnectRoute(Outbound, self)
        log.debug(s" --> ${ref.path.name}")

      case ConnectRoute(Outbound, ref) =>
        downstream set ref
        log.debug(s" <-- ${ref.path.name}")

      case Tcp.Received(data) =>
        downstream ! demux(data)

      case Envelope(msgs, _) =>
        msgs foreach writeOut

      case _: Tcp.ConnectionClosed =>
        log.info("Stopping, because connection for remote address {} closed", remote)
        context.stop(self)

      case Terminated(`connection`) =>
        log.info("Stopping, because connection for remote address {} died", remote)
        context.stop(self)
    }
  }
}
