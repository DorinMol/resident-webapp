package service

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.kinesis.scaladsl.KinesisSource
import akka.stream.alpakka.kinesis.{ShardIterator, ShardSettings}
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.ByteString
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import config.ConfigEvent
import dto.DtoAverageEvent
import javax.inject.{Inject, Singleton}
import model.ModelEvent
import play.api.Logger
import play.api.libs.json.Json
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.awssdk.services.kinesis.model.Record

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ServiceConsumer @Inject()()(implicit val config: ConfigEvent, actorSystem: ActorSystem, materializer: Materializer, ec: ExecutionContext) {
  private val logger = Logger(getClass)

  private implicit val amazonKinesisAsync: KinesisAsyncClient =
    KinesisAsyncClient
      .builder()
      .region(Region.US_EAST_1)
      .httpClient(AkkaHttpClient.builder().withActorSystem(actorSystem).build())
      .build()
  actorSystem.registerOnTermination(amazonKinesisAsync.close())

  private val settings =
    ShardSettings(streamName = config.streamName, "shardId-000000000000")
      .withRefreshInterval(1.second)
      .withLimit(10000)
      .withShardIterator(ShardIterator.TrimHorizon)

  def average(eventType: String, from: Long, to: Long): Future[DtoAverageEvent] = {
//    val source: Source[software.amazon.awssdk.services.kinesis.model.Record, NotUsed] =
//      KinesisSource.basic(settings, amazonKinesisAsync)

//    val sink: Sink[Option[ModelEvent], Future[DtoAverageEvent]] =
//      Sink.fold(DtoAverageEvent(eventType, 0d, 0)){ (a, b) =>
//      a.copy(
//        value = a.value + b.get.value,
//        processedCount = a.processedCount + 1)
//      }
//    source
//      .via(recordToEvent)
//      .filter { me =>
//        me.isDefined &&
//          me.get.eventType == eventType &&
//          me.get.timestamp >= from &&
//          me.get.timestamp <= to
//      }
//      .runWith(sink)
//      .map(ae =>
//        DtoAverageEvent(ae.eventType, (ae.value / ae.processedCount), ae.processedCount)
//      )

    val source = FileIO.fromPath(Paths.get("resources/resident-samples.log"))
    val sink: Sink[DtoAverageEvent, Future[DtoAverageEvent]] = Sink.seq


    source
      .via(bytesStringToString)
      .via(splitString)
      .fold(DtoAverageEvent(eventType, 0, 0)) {
        (dae, modelEvents) =>
          val filteredEvents = modelEvents.filter { me =>
              me.eventType == eventType &&
              me.timestamp >= from &&
              me.timestamp <= to
          }
          dae.copy(
            value = dae.value + filteredEvents.map(_.value).sum,
            processedCount = dae.processedCount + filteredEvents.length)
      }
      .map(ae =>
        DtoAverageEvent(ae.eventType, (ae.value / ae.processedCount), ae.processedCount)
      )
  }

  private def recordToEvent: Flow[Record, Option[ModelEvent], NotUsed] = {
    Flow[Record]
      .map { record =>
        val bytes = record.data().asByteArray()
        val str = new String(bytes)
        Json.parse(str).validate[ModelEvent].asOpt
      }
  }

  private def bytesStringToString: Flow[ByteString, String, NotUsed] = {
    Flow[ByteString]
      .map { byteString =>
        byteString.utf8String
      }
  }

  private def splitString: Flow[String, List[ModelEvent], NotUsed] = {
    Flow[String]
      .map { string =>
        string.split("\n").toList
          .flatMap { line =>
            line.split(",").toList match {
              case timestamp :: eventType :: value :: Nil =>
                Some(ModelEvent(eventType, timestamp.toLong, value.toDouble))
              case _ =>
                None
            }
          }
      }
  }
}
