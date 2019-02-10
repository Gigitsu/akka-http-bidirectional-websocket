package it.gsquare.ws

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * created by gigitsu on 10/02/2019.
  */
class WsHandlerActor private (implicit ec: ExecutionContext) extends Actor {
  import WsHandlerActor._

  implicit val as: ActorSystem = context.system
  implicit val am: ActorMaterializer = ActorMaterializer()

  val (down, publisher) = Source.
    actorRef[String](1000, OverflowStrategy.fail).
    toMat(Sink.asPublisher(fanout = false))(Keep.both).
    run()

  val outbound: Source[TextMessage.Strict, NotUsed] = Source.fromPublisher(publisher).map(TextMessage.Strict)
  val inbound: Sink[Message, Any] = Flow[Message].mapAsync(1) {
    case x: TextMessage => x.toStrict(3.seconds).map(_.text)
    case x: BinaryMessage =>
      x.dataStream.runWith(Sink.ignore)
      Future.failed(new Exception("Unexpected data"))
  } to Sink.foreach[String](self ! _)

  override def receive: Receive = {
    case InitWebSocket => sender ! Flow.fromSinkAndSource(inbound, outbound)
    case x: String =>
      println(x)
      down ! s"Hello, $x"
  }
}

object WsHandlerActor {
  case class InitWebSocket()
  def props(implicit ec: ExecutionContext) = Props(new WsHandlerActor)
}
