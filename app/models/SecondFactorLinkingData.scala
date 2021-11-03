package models

import play.api.libs.json.{Format, Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, Macros}

/** Data for linking user to second factor.
 *
 * @param hashId      id for linking
 * @param twoFactorId id of second factor
 * @param secretKey   key for verifying a second factor signing in. */
case class SecondFactorLinkingData(
                                    hashId: String,
                                    twoFactorId: Option[String],
                                    secretKey: Option[String],
                                  )

/** A companion object which contains implicits for json converting . */
object SecondFactorLinkingData {

  import reactivemongo.play.json.compat
  import compat.bson2json._
  import compat.json2bson._

  implicit val reader: BSONDocumentReader[SecondFactorLinkingData] = Macros.reader[SecondFactorLinkingData]
  implicit val jsonFormat: Format[SecondFactorLinkingData] = Json.format[SecondFactorLinkingData]
  implicit val format: OFormat[SecondFactorLinkingData] = Json.format[SecondFactorLinkingData] // for mongodb
}
