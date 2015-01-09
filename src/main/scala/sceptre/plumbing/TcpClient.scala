package sceptre.plumbing

import java.net.InetSocketAddress

import akka.actor._
import akka.io.Tcp.{CommandFailed, Register, Connected, Connect}
import akka.io.{Tcp, IO}
import akka.util.ByteString
import rx.lang.scala.{Notification, Observer, Subscriber, Observable}


class TcpClient(name: String, addr: InetSocketAddress)(implicit system: ActorSystem) {

  def start: Observable[TcpConnection] =
    Observable { subscriber =>
      system.actorOf(Props(new Connector(subscriber)), s"$name-connector")
    }


  class Connector(connsubscriber: Subscriber[TcpConnection]) extends Actor with ActorLogging {

    log.info(s"connecting to $addr")
    IO(Tcp) ! Connect(addr)


    def receive: Receive = {
      case Connected(remote, _) =>
        log.info("address {} connected", remote)
        val sdr = sender()
        log.info(s"connected sender = $sdr")


        val connInput: Observable[ByteString] = Observable { subscriber =>
          val handler = context.actorOf( Props( new Handler(remote, sdr, subscriber) ) )
          sdr ! Register(handler)
        }.toBlocking

        connsubscriber.onNext(new TcpConnection{
          override val output: Observer[ByteString] = Observer(
            onNext = (data) => sdr ! Send(data),
            onError = (_: Throwable) => {},
            onCompleted = () => ()
          )
          override val input: Observable[ByteString] = connInput
        })

      case cf @ CommandFailed(b: Connect) =>
        connsubscriber.onError(new RuntimeException(cf.toString))
        log.error("TCP connect failed!")
      case other => log.warning(s"$other")
    }
  }

  case class Send(data: ByteString)

  class Handler (remote: InetSocketAddress, connection: ActorRef, subscriber: Subscriber[ByteString]) extends Actor with ActorLogging {
    val rx = context.actorOf(Props(new RxActor(subscriber)) )

    // We need to know when the connection dies without sending a `Tcp.ConnectionClosed`
    context.watch(connection)

    log.info(s"connection = $connection")

    val downstream = DeferredActorRef()(context)

    def receive: Receive = {
      case Tcp.Received(data) =>
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