name := "auctionhouse"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "com.typesafe.akka" %% "akka-testkit"  % "2.3.7"% "test"
)