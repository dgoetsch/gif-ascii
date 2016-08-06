import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayScala
import sbt._
import Keys._
import play.sbt.PlayImport._

//fork := true
//routesGenerator := InjectedRoutesGenerator

val appName = "image-ascii"
val appVersion = "1.0"
val akkaVersion  = "2.4.8"

val appDependencies = Seq(
  ws,
  "commons-codec" % "commons-codec" % "1.7",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.google.inject" % "guice" % "4.0",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "org.scalaz" % "scalaz-core_2.11" % "7.1.6",
  "org.scalaz.stream" % "scalaz-stream_2.11" % "0.8",
  "jline" % "jline" % "2.11",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.4.1",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  specs2 % Test
)

lazy val cassandraDependencies = Seq (
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.0"
)

maintainer in Docker := "Devyn Goetsch devynmichellegoetsch@gmail.com"
packageSummary in Docker := "Turns gifs into ascii"
packageDescription := "Docker image injest service"
dockerExposedPorts in Docker := Seq(9000, 9443)

// Only add this if you want to rename your docker image name
packageName in Docker := "image-injest"

val main = Project(appName, file("."))
  .enablePlugins(PlayScala)
  //.enablePlugins(SbtNativePackager)
  .settings(
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-Ywarn-dead-code",
      "-language:_",
      "-target:jvm-1.7",
      "-encoding", "UTF-8"
  ),
  libraryDependencies ++= appDependencies ++ cassandraDependencies,
  version := appVersion
).settings()
