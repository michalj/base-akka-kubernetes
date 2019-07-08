package base

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class DummyDictionary(implicit val executionContext: ExecutionContext) extends Dictionary {
  private val inner = new mutable.HashMap[String, String]()

  override def get(key: String): Future[String] = {
    Future.apply(inner.getOrElse(key, ""))
  }

  override def set(key: String, value: String): Future[Unit] = {
    Future.apply {
      inner.put(key, value)
    }
  }
}
