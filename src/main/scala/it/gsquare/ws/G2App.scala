package it.gsquare.ws

/**
  * created by gigitsu on 08/02/2019.
  */
object G2App extends App {
  override def main(args: Array[String]): Unit = {
    val ws = new G2WsServer(println)

    for (ln <- scala.io.Source.stdin.getLines) ln match {
      case "" =>
        ws.terminate()
        return
      case x => ws.sendMessage(x)
    }
  }
}
