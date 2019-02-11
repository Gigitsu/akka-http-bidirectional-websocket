package it.gsquare.ws

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer

/**
  * created by gigitsu on 10/02/2019.
  */
class WsHandlerActor private(down: ActorRef) extends Actor with ActorLogging {
  implicit val as: ActorSystem = context.system
  implicit val am: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = {
    case x: Int =>
      log.info(s"message received: [$x]")
      down ! x.toString
    case x: String =>
      log.info(s"message received: [$x]")
      down ! s"Hello, $x"
  }
}

object WsHandlerActor {
  def props(down: ActorRef) = Props(new WsHandlerActor(down))
}
