package models

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import play.api.libs.json.{Format, Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, BSONObjectID, Macros}

case class User(
                 _id: Option[BSONObjectID],
                 credentialProviderId: String,
                 email: String,
                 name: String,
                 lastName: String,
                 password: Option[String]
               ) extends Identity{

  def loginInfo = LoginInfo(CredentialsProvider.ID, email)

  def passwordInfo = PasswordInfo(BCryptSha256PasswordHasher.ID, password.get)

}

object User {

  import reactivemongo.play.json.compat
  import compat.bson2json._
  import compat.json2bson._

//  implicit val passwordInfoJsonFormat: OFormat[PasswordInfo] = Json.format[PasswordInfo]
  implicit val userReader: BSONDocumentReader[User] = Macros.reader[User]
  implicit val userJsonFormat: Format[User] = Json.format[User]
  implicit val userFormat: OFormat[User] = Json.format[User] // for mongodb
}

