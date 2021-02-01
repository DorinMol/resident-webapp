package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class ConfigEvent @Inject()()(implicit val applicationConfig: Configuration) {
  val streamName: String = applicationConfig.get[String]("kinesis-consumer.stream-name")
  val parallelism: Int = applicationConfig.get[Int]("kinesis-consumer.parallelism")
}
