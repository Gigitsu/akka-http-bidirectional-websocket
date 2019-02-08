package it.gsquare.ws

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.ActorMaterializer
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

/**
  * created by gigitsu on 08/02/2019.
  */
class G2WsServerSpec extends FlatSpec with Matchers with Directives with ScalatestRouteTest with MockFactory {
  implicit val as: ActorSystem = ActorSystem("G2Spec")
  implicit val am: ActorMaterializer = ActorMaterializer()

  val f = mockFunction[Any, Unit]
  val ws = new G2WsServer(f)

  behavior of "G2 WebSocket"

  it should "send a message" in {
    // create a testing probe representing the client-side
    val wsClient = WSProbe()

    WS("/ws", wsClient.flow) ~> ws.routes ~> check {
      // check response from ws upgrade headers
      isWebSocketUpgrade shouldBe true

      ws.sendMessage("42")
      wsClient.expectMessage("42")

      ws.sendMessage("23")
      wsClient.expectMessage("23")

      f.expects(TextMessage.Strict("23"))
      f.expects(TextMessage.Strict("42"))

      wsClient.sendMessage("23")
      wsClient.sendMessage("42")
    }
  }
}
