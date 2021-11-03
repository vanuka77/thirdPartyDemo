package models

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import play.api.libs.json.{Format, Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentReader, BSONObjectID, Macros}

/** A user who uses this application.
 *
 * @param _id                     id in database.
 * @param credentialProviderId    a provider for authenticating with credentials.
 * @param email                   user email.
 * @param name                    user bane.
 * @param lastName                user last name.
 * @param password                user password.
 * @param authenticatorId         id of authenticator.
 * @param isLinkedToSecondFactor  if user is linked to second factor or not.
 * @param secondFactorLinkingData data for linking user to second factor.
 * */
case class User(
                 _id: Option[BSONObjectID],
                 credentialProviderId: String,
                 email: String,
                 name: String,
                 lastName: String,
                 password: Option[String],
                 authenticatorId: Option[String],
                 isLinkedToSecondFactor: Boolean,
                 secondFactorLinkingData: Option[SecondFactorLinkingData]
               ) extends Identity {

  /** Represents a linked login for an identity (email/password).
   * The login info contains the data about the provider that authenticated that identity. */
  def loginInfo = LoginInfo(CredentialsProvider.ID, email)

  /** The password details. */
  def passwordInfo = PasswordInfo(BCryptSha256PasswordHasher.ID, password.get)

}

/** A companion object which contains implicits for json converting . */
object User {

  import reactivemongo.play.json.compat
  import compat.bson2json._
  import compat.json2bson._

  implicit val reader: BSONDocumentReader[User] = Macros.reader[User]
  implicit val jsonFormat: Format[User] = Json.format[User]
  implicit val format: OFormat[User] = Json.format[User] // for mongodb
}

