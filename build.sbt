name := """resident-webapp"""
organization := "com.resident"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "com.lightbend.akka" %% "akka-stream-alpakka-kinesis" % "2.0.2",
  "com.typesafe.akka" %% "akka-http" % "10.1.13",
  "ch.qos.logback"             %  "logback-core"             % "1.2.3",
  "ch.qos.logback"             %  "logback-classic"          % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.2"
)

scalacOptions := Seq(
  "-deprecation",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Wdead-code",
  "-Wunused:imports",
  "-Ymacro-annotations"
)