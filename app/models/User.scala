package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.Json
import reactivemongo.api.bson.{BSONObjectID, Macros}

import java.util.UUID

case class User(
                 _id: Option[BSONObjectID],
                 id: UUID,
                 credentialProviderId: String,
                 email: String,
                 name: String,
                 lastName: String,
                 password: Option[String] = None
               ) extends Identity {

  def loginInfo: LoginInfo = LoginInfo(credentialProviderId, email)
}

object User {

  import reactivemongo.play.json.compat
  import compat.bson2json._
  import compat.json2bson._

  implicit val userReader = Macros.reader[User]
  implicit val userFormat = Json.format[User]
}

