package it.gsquare.ws

/**
  * created by gigitsu on 08/02/2019.
  */
object WsApp extends App {
  val ws = new WsServer

  for (ln <- scala.io.Source.stdin.getLines) ln match {
    case "" =>
      ws.terminate()
      System.exit(0)
    case x => ws.sendMessage(x)
  }
}
