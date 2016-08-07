import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayScala
import sbt._
import Keys._
import play.sbt.PlayImport._

val appVersion = "1.0"

val akkaVersion  = "2.4.8"
val cassandraDriverVersion = "3.1.0"

val appDependencies = Seq(
  "commons-codec" % "commons-codec" % "1.7",
  "com.google.inject" % "guice" % "4.0",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "org.scalaz" % "scalaz-core_2.11" % "7.1.6",
  "org.scalaz.stream" % "scalaz-stream_2.11" % "0.8",
  "jline" % "jline" % "2.11",
  ws,
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  specs2 % Test
)

val playDependencies = Seq(
  "com.typesafe.play" %% "play" % "2.5.4"
)

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
)

lazy val cassandraDependencies = Seq (
  "com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverVersion
)

lazy val commonSettings = Seq(
  scalaVersion := "2.11.7",
  version := appVersion,
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-Xlint",
    "-Ywarn-dead-code",
    "-language:_",
    "-target:jvm-1.7",
    "-encoding", "UTF-8"
  )
)

lazy val root = (project in file("."))
  .aggregate(common, injest)

lazy val common = (project in file("common")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= cassandraDependencies ++ akkaDependencies ++ playDependencies
  )

lazy val injest = (project in file("injest")).
  enablePlugins(PlayScala).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= appDependencies,
    maintainer in Docker := "Devyn Goetsch devynmichellegoetsch@gmail.com",
    packageSummary in Docker := "Turns gifs into ascii",
    packageDescription := "Docker image injest service",
    dockerExposedPorts in Docker := Seq(9000, 9443),
    packageName in Docker := "image-injest"
  ).
  dependsOn(common)
