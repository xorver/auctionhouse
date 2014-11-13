name := "auctionhouse"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "com.typesafe.akka" %% "akka-testkit"  % "2.2-M3"% "test",
  "ch.qos.logback" % "logback-classic" % "1.0.7"
)

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"