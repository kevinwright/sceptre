package sceptre.plumbing

import rx.lang.scala.{Subscription, Observable}

trait Pipable[MsgType] {
  type Result
  def pipeFrom(term: Terminus[MsgType]): Result
}

object Pipable {
  type Aux[MsgT, ResultT] = Pipable[MsgT]{type Result = ResultT}

  implicit class SimpleFnIsPipable[T](fn: T => T) extends Pipable[T] {
    type Result = Terminus[T]

    def pipeFrom(term: Terminus[T]): Terminus[T] = {
      AdaptedTerminus(
        source = term.source map fn,
        sink = term.sink,
        connect = term.connect
      )
    }
  }

  implicit class FnToObservableIsPipable[T](fn: T => Observable[T]) extends Pipable[T] {
    type Result = Terminus[T]

    def pipeFrom(term: Terminus[T]): Terminus[T] = {
      AdaptedTerminus(
        source = term.source concatMap fn,
        sink = term.sink,
        connect = term.connect
      )
    }
  }

  implicit class FnFromResponseIsPipable[T](fn: Response[T] => T) extends Pipable[T] {
    type Result = Terminus[T]

    def pipeFrom(term: Terminus[T]): Terminus[T] = {
      AdaptedTerminus(
        source = term.source map { msg => fn(Reply(msg)) },
        sink = (obs) => term.sink(obs map { msg => fn(Output(msg)) }),
        connect = term.connect
      )
    }
  }

  implicit class FnToObservableResponseIsPipable[T](fn: Observable[T] => Observable[Response[T]]) extends Pipable[T] {
    type Result = Terminus[T]

    def pipeFrom(term: Terminus[T]): Terminus[T] = {
      val responses = fn(term.source)
      val outputs = responses collect { case Output(m) => m}
      val replies = responses collect { case Reply(m) => m}

      AdaptedTerminus(
        source = outputs,
        sink = (x: Observable[T]) => term.sink(x merge replies),
        connect = term.connect
      )
    }
  }

  implicit class TerminusIsPipable[T](destTerminus: Terminus[T]) extends Pipable[T] {
    type Result = Subscription

    def pipeFrom(srcTerminus: Terminus[T]): Subscription = {
      val sub = destTerminus sink srcTerminus.source
      srcTerminus.connect()
      sub
    }
  }
}
