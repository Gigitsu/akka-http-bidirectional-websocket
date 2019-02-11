package it.gsquare.ws

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill, Terminated}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * created by gigitsu on 08/02/2019.
  */
class WsServer {
  private implicit val as: ActorSystem = ActorSystem("G2WS")
  private implicit val am: ActorMaterializer = ActorMaterializer()

  private implicit val ec: ExecutionContext = as.dispatcher

  private val mediator: ActorRef = DistributedPubSub(as).mediator

  val routes: Route = pathEndOrSingleSlash {
    complete("G2 web socket server up and running")
  } ~ path("ws") {
    println("new incoming connection")

    val (down, publisher) = Source.
      actorRef[String](1000, OverflowStrategy.fail).
      toMat(Sink.asPublisher(fanout = false))(Keep.both).
      run()

    val handler = as.actorOf(WsHandlerActor.props(down))

    val outbound: Source[TextMessage.Strict, NotUsed] = Source.fromPublisher(publisher).map(TextMessage.Strict)
    val inbound: Sink[Message, Any] = Flow[Message].mapAsync(1) {
      case x: TextMessage => x.toStrict(3.seconds).map(_.text)
      case x: BinaryMessage =>
        x.dataStream.runWith(Sink.ignore)
        Future.failed(new Exception("Unexpected data"))
    } to Sink.actorRef[String](handler, PoisonPill)

    println("start web socket")
    handleWebSocketMessages(Flow.fromSinkAndSource(inbound, outbound))
  }

  def sendMessage(s: Any): Unit = {
    mediator ! Publish("notifications", s)
  }

  def terminate(): Future[Terminated] = as.terminate()

  Http().bindAndHandle(routes, "0.0.0.0", 8023).onComplete {
    case Success(value) => println(value)
    case Failure(exception) => println(exception)
  }
}
