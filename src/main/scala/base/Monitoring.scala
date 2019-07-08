package base

import io.prometheus.client.Histogram
import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.hotspot.DefaultExports

import scala.concurrent.{ExecutionContext, Future}

object Monitoring {
  DefaultExports.initialize()
  val metricGets: Histogram = Histogram.build()
    .exponentialBuckets(1, 2, 10)
    .name("dictionary_get")
    .help("Dictionary GETs")
    .register()
  val metricPuts: Histogram = Histogram.build()
    .exponentialBuckets(1, 2, 10)
    .name("dictionary_put")
    .help("Dictionary PUTs")
    .register()

  new HTTPServer(8081)

  def monitored[T](metric: Histogram, query: Future[T])(implicit context: ExecutionContext): Future[T] = {
    val timer = metric.startTimer()
    query.map(result => {
      timer.observeDuration()
      result
    })
  }
}
