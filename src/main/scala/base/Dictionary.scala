package base

import scala.concurrent.Future

trait Dictionary {
  def get(key: String): Future[String]
  def set(key: String, value: String): Future[Unit]
}
