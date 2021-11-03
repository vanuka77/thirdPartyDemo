package models.chat

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, Macros}

/** A model for communication between websockets and system event stream.
 *
 * @see [[controllers.websocket.AuthenticationController]],[[controllers.websocket.LinkController]]
 * */
case class SecondFactorMessage(operationType: String)

/** A companion object which contains implicits for json converting . */
object SecondFactorMessage {

  implicit val reader: BSONDocumentReader[SecondFactorMessage] = Macros.reader[SecondFactorMessage]
  implicit val format: OFormat[SecondFactorMessage] = Json.format[SecondFactorMessage]
}