package it.gsquare.ws

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * created by gigitsu on 08/02/2019.
  */
class G2WsServer(f: Any => Unit) {
  private implicit val as: ActorSystem = ActorSystem("G2WS")
  private implicit val am: ActorMaterializer = ActorMaterializer()

  private implicit val ec: ExecutionContext = as.dispatcher

  private var connections: List[ActorRef] = List()

  private def listen(): Flow[Message, Message, NotUsed] = {
    val outbound: Source[Message, ActorRef] = Source.actorRef[Message](1000, OverflowStrategy.fail)
    val inbound: Sink[Message, Any] = Sink.foreach(f)

    Flow.fromSinkAndSourceMat(inbound, outbound)((_, outboundMat) => {
      connections ::= outboundMat
      NotUsed
    })
  }

  val routes: Route = pathEndOrSingleSlash {
    complete("G2 web socket server up and running")
  } ~ path("ws") {
    handleWebSocketMessages(listen())
  }

  def sendMessage(s: String): Unit = {
    for (con <- connections) con ! TextMessage.Strict(s)
  }

  def terminate(): Future[Terminated] = as.terminate()

  Http().bindAndHandle(routes, "0.0.0.0", 8023).onComplete {
    case Success(value) => println(value)
    case Failure(exception) => println(exception)
  }
}
