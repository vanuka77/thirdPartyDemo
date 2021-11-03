package models

import play.api.libs.json.{Format, Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, Macros}

/** Data for handling a signing in for second factor.
 *
 * @param twoFactorId id of second factor
 * @param totpPassword   temp password for verifying a signing in.  */
case class SecondFactorLoginData(
                                  twoFactorId: String,
                                  totpPassword: Int,
                                )
/** A companion object which contains implicits for json converting . */
object SecondFactorLoginData {

  import reactivemongo.play.json.compat
  import compat.bson2json._
  import compat.json2bson._

  implicit val reader: BSONDocumentReader[SecondFactorLoginData] = Macros.reader[SecondFactorLoginData]
  implicit val jsonFormat: Format[SecondFactorLoginData] = Json.format[SecondFactorLoginData]
  implicit val format: OFormat[SecondFactorLoginData] = Json.format[SecondFactorLoginData] // for mongodb
}
