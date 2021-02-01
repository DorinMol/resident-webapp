package model

import play.api.libs.json.{Json, Reads}

case class ModelEvent(eventType: String, timestamp: Long, value: Double)

object ModelEvent {
  implicit val jsonFormat: Reads[ModelEvent] = Json.reads[ModelEvent]
}
