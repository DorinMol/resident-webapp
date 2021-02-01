package dto

import play.api.libs.json.{Format, Json}

case class DtoAverageEvent(eventType: String, value: Double, processedCount: Int)

object DtoAverageEvent {
  implicit val jsonFormat: Format[DtoAverageEvent] = Json.format[DtoAverageEvent]
}