package base

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.{Cluster, ClusterEvent}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import io.prometheus.client.hotspot.DefaultExports

object DemoApp extends App {

  implicit val system = ActorSystem("Base")

  import system.log
  implicit val mat = ActorMaterializer()
  val cluster = Cluster(system)

  log.info(s"Started [$system], cluster.selfAddress = ${cluster.selfAddress}")

  DefaultExports.initialize()
  val requestTime = io.prometheus.client.Histogram.build()
    .exponentialBuckets(1, 2, 10)
    .name("http_requests")
    .help("HTTP requests")
    .register()

  import io.prometheus.client.exporter.HTTPServer

  val metricsServer = new HTTPServer(8081)

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  cluster.subscribe(
    system.actorOf(Props[ClusterWatcher]),
    ClusterEvent.InitialStateAsEvents,
    classOf[ClusterDomainEvent]
  )

  // add real app routes here
  val routes =
    path(Segments) { p =>
      get {
        complete(
          {
            requestTime.observe(0.0)
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              s"<h1>Hello: $p</h1>"
            )
          }
        )
      }
    }

  Http().bindAndHandle(routes, "0.0.0.0", 8080)

  Cluster(system).registerOnMemberUp({
    log.info("Cluster member is up!")
  })
}

class ClusterWatcher extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  override def receive = {
    case msg ⇒ log.info(s"Cluster ${cluster.selfAddress} >>> " + msg)
  }
}
