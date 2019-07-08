package base

import java.util.concurrent.TimeUnit

import akka.actor.Scheduler
import akka.actor.typed.scaladsl.{AskPattern, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.util.Timeout

import scala.concurrent.Future

class ShardedDictionary(sharding: ClusterSharding)(implicit val scheduler: Scheduler) extends Dictionary {
  private implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)

  private val TypeKey = EntityTypeKey[ShardedDictionary.Command]("Dictionary")
  private val shardRegion: ActorRef[ShardingEnvelope[ShardedDictionary.Command]] = sharding.init(Entity(
    typeKey = TypeKey,
    createBehavior = _ => ShardedDictionary.inMemoryDictionary(Map())
  ))

  override def get(key: String): Future[String] = {
    import AskPattern._

    shardRegion ? (replyTo => ShardingEnvelope(shardId(key), ShardedDictionary.Get(key, replyTo)))
  }

  override def set(key: String, value: String): Future[Unit] = {
    shardRegion.tell(ShardingEnvelope(shardId(key), ShardedDictionary.Set(key, value)))
    Future.successful(())
  }

  private def shardId(key: String) = {
    math.abs(key.hashCode % 16).toString
  }
}

object ShardedDictionary {
  private def inMemoryDictionary(data: Map[String, String]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case Set(key, value) => inMemoryDictionary(data.updated(key, value))
      case Get(key, replyTo) =>
        replyTo.tell(data.getOrElse(key, ""))
        Behaviors.same
    }
  }

  private trait Command
  private case class Set(key: String, value: String) extends Command
  private case class Get(Key: String, respondTo: ActorRef[String]) extends Command
}
