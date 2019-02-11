package it.gsquare.ws

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck, Unsubscribe}

/**
  * created by gigitsu on 10/02/2019.
  */
class WsHandlerActor private(token: String, down: ActorRef) extends Actor with ActorLogging {
  private val mediator: ActorRef = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = mediator ! Subscribe("notifications", self)

  override def postStop(): Unit = {
    mediator ! Unsubscribe
    log.info("actor stopped")
  }

  override def receive: Receive = {
    case SubscribeAck(Subscribe(topic, None, _)) =>
      log.info("Subscribed {} to {}", token, topic)
    case x: Int =>
      log.info(s"message received: [$x]")
      down ! x.toString
    case x: String =>
      log.info(s"message received: [$x]")
      down ! s"Hello, $x"
  }
}

object WsHandlerActor {
  def props(token: String, down: ActorRef) = Props(new WsHandlerActor(token, down))
}
