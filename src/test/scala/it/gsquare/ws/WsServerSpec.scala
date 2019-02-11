package it.gsquare.ws

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.ActorMaterializer
import org.scalatest.{FlatSpec, Matchers}

/**
  * created by gigitsu on 08/02/2019.
  */
class WsServerSpec extends FlatSpec with Matchers with Directives with ScalatestRouteTest {
  implicit val as: ActorSystem = ActorSystem("G2Spec")
  implicit val am: ActorMaterializer = ActorMaterializer()

  val ws = new WsServer

  behavior of "G2 WebSocket"

  it should "send a message" in {
    // create a testing probe representing the client-side
    val wsClient = WSProbe()

    WS("/ws", wsClient.flow) ~> ws.routes ~> check {
      // check response from ws upgrade headers
      isWebSocketUpgrade shouldBe true

      ws.sendMessage(42)
      wsClient.expectMessage("42")

      ws.sendMessage(23)
      wsClient.expectMessage("23")

      wsClient.sendMessage("Gigi")
      wsClient.expectMessage("Hello, Gigi")

      wsClient.sendMessage("Luca")
      wsClient.expectMessage("Hello, Luca")
    }
  }
}
