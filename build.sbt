name := "akka-http-bidirectional-websocket"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.16",

  // TEST
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.scalamock" %% "scalamock" % "4.1.0" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test
)