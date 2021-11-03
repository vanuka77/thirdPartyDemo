package models.json

import play.api.libs.json.Json

/** A model for communication with websockets between front end and back end. */
case class WebSocketAction(action: String, answer: Option[String] = None)

/** A companion object which contains implicits for json converting . */
object WebSocketAction {
  implicit val webSocketAFormat = Json.format[WebSocketAction]
}
