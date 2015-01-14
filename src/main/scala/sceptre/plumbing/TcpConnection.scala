package sceptre.plumbing

import java.net.InetSocketAddress
import akka.actor._
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import rx.lang.scala._

import scala.reflect.ClassTag



private[plumbing] object Endpoint {
  sealed trait EndpointType
  case object Client extends EndpointType
  case object Server extends EndpointType
}

import Endpoint._

private[plumbing] class TcpEndpoint(endpointType: EndpointType, name: String, addr: InetSocketAddress)(implicit system: ActorSystem) {

  def start: Observable[Terminus[ByteString]] =
    Observable { subscriber =>
      system.actorOf(Props(new Connector(subscriber)))
    }


  class Connector(connsubscriber: Subscriber[Terminus[ByteString]]) extends Actor with ActorLogging {

    endpointType match {
      case Client =>
        log.info (s"connecting to $addr")
        IO (Tcp) ! Connect (addr)
      case Server =>
        log.info(s"awaiting connections on $addr")
        IO(Tcp) ! Bind(context.self, addr)
    }

    def receive: Receive = {
      case Connected(remoteAddr, _) =>
        log.info("address {} connected", remoteAddr)
        val remoteActor = sender()
        log.info(s"connected sender = $remoteActor")

        connsubscriber.onNext(new Terminus[ByteString]{
          val sink = (source: Observable[ByteString]) => source.subscribe(
            onNext = (data: ByteString) => remoteActor ! Tcp.Write(data),
            onError = (_: Throwable) => {},
            onCompleted = () => ()
          )

          val source = Observable[ByteString] { subscriber =>
            val handler = context.actorOf( Props( new Handler(remoteAddr, remoteActor, subscriber) ) )
            remoteActor ! Register(handler)
          }.publish

          val connect = () => source.connect
        })

      case cf @ CommandFailed(b: Connect) =>
        connsubscriber.onError(new RuntimeException(cf.toString))
        log.error("TCP connect failed!")

      case cf @ CommandFailed(b: Bind) =>
        connsubscriber.onError(new RuntimeException(cf.toString))
        log.error("TCP bind failed!")

      case other => log.warning(s"$other")
    }
  }

  class RxActor[T: ClassTag](subscriber: Subscriber[T]) extends Actor with ActorLogging {
    def receive: Receive = {
      case Notification.OnNext(value: T) => subscriber.onNext(value)
      case Notification.OnCompleted => subscriber.onCompleted()
      case Notification.OnError(value) => subscriber.onError(value)
    }
  }

  class Handler (remote: InetSocketAddress, connection: ActorRef, subscriber: Subscriber[ByteString]) extends Actor with ActorLogging {
    val rx = context.actorOf(Props(new RxActor(subscriber)) )

    // We need to know when the connection dies without sending a `Tcp.ConnectionClosed`
    context.watch(connection)

    log.info(s"connection = $connection")

    def receive: Receive = {
      case Tcp.Received(data) =>
        //log.info(name + " input bytes " + data.iterator.map(_.toInt & 0xFF).mkString("[", ",", "]"))
        rx ! Notification.OnNext(data)

      case _: Tcp.ConnectionClosed =>
        log.info("Stopping, because connection for remote address {} closed", remote)
        rx ! Notification.OnCompleted
        context.stop(self)

      case Terminated(`connection`) =>
        log.info("Stopping, because connection for remote address {} died", remote)
        rx ! Notification.OnCompleted
        context.stop(self)
    }
  }
}

class TcpClient(name: String, addr: InetSocketAddress)(implicit system: ActorSystem)
  extends TcpEndpoint(Client, name, addr)

class TcpServer(name: String, addr: InetSocketAddress)(implicit system: ActorSystem)
  extends TcpEndpoint(Server, name, addr)

