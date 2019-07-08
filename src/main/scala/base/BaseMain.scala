package base

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.{Cluster, ClusterEvent}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import base.Monitoring.monitored

object DemoApp extends App {
  implicit val system = ActorSystem("Base")
  implicit val executionContext = system.dispatcher
  implicit val scheduler = system.scheduler

  import system.log

  implicit val mat = ActorMaterializer()
  val cluster = Cluster(system)

  log.info(s"Started [$system], cluster.selfAddress = ${cluster.selfAddress}")

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  cluster.subscribe(
    system.actorOf(Props[ClusterWatcher]),
    ClusterEvent.InitialStateAsEvents,
    classOf[ClusterDomainEvent]
  )

  val sharding = ClusterSharding(system.toTyped)

  //val dictionary = new DummyDictionary()
  val dictionary = new ShardedDictionary(sharding)

  val routes =
    path("dictionary" / Segment) { key =>
      get {
        onSuccess(monitored(Monitoring.metricGets, dictionary.get(key))) {
          complete(_)
        }
      } ~
      put {
        entity(as[String]) { value =>
          onSuccess(monitored(Monitoring.metricPuts, dictionary.set(key, value))) {
            complete("stored")
          }
        }
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
