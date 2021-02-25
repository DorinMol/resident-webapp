package service

import java.nio.file.Paths
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString
import config.ConfigEvent
import javax.inject.{Inject, Singleton}
import model.{AccumulatorEvent, ModelEvent}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ServiceConsumer @Inject()()(implicit val config: ConfigEvent, actorSystem: ActorSystem, materializer: Materializer, ec: ExecutionContext) {

  def average(eventType: String, from: Long, to: Long): Future[AccumulatorEvent] = {
    val source = FileIO.fromPath(Paths.get("resources/resident-samples.log"))
    val sink: Sink[AccumulatorEvent, Future[AccumulatorEvent]] =
      Sink.head

    source
      .via(bytesStringToString)
      .via(splitString)
      .fold(AccumulatorEvent(0, 0)) {
        (dae, modelEvents) =>
          val filteredEvents = modelEvents.filter { me =>
            me.eventType == eventType &&
              me.timestamp >= from &&
              me.timestamp <= to
          }
          dae.copy(
            accValue = dae.accValue + filteredEvents.map(_.value).sum,
            accNumber = dae.accNumber + filteredEvents.length)
      }
      .runWith(sink)
  }

  private def bytesStringToString: Flow[ByteString, String, NotUsed] = {
    Framing
      .delimiter(ByteString(System.lineSeparator()), maximumFrameLength = 512, allowTruncation = true)
      .map (_.utf8String)
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
