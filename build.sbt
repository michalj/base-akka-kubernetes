import com.typesafe.sbt.packager.docker._

name := "base-akka-kubernetes"

version := "0.1"

scalaVersion := "2.12.8"

val AkkaVersion = "2.5.21"
val AkkaHttpVersion = "10.1.8"
val SprayJsonVersion = "1.3.5"
val PrometheusClient = "0.6.0"

enablePlugins(JavaServerAppPackaging)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "com.lightbend.akka.management" %% "akka-management" % "1.0.1",
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % "1.0.1",

  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.1",
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.1",
  "io.spray" %% "spray-json" % SprayJsonVersion,
  "io.prometheus" % "simpleclient" % PrometheusClient,
  "io.prometheus" % "simpleclient_hotspot" % PrometheusClient,
  "io.prometheus" % "simpleclient_httpserver" % PrometheusClient
)

enablePlugins(JavaServerAppPackaging)

dockerExposedPorts := Seq(8080, 8558, 2552)
dockerBaseImage := "openjdk:13-alpine"
dockerRepository := Some("localhost:30001")

dockerCommands ++= Seq(
  Cmd("USER", "root"),
  Cmd("RUN", "/sbin/apk", "add", "--no-cache", "bash", "bind-tools", "busybox-extras", "curl", "strace"),
  Cmd("RUN", "chgrp -R 0 . && chmod -R g=u .")
)
