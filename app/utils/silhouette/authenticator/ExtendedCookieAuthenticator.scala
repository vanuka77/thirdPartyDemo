package utils.silhouette.authenticator

import com.mohiva.play.silhouette.api.crypto.{AuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.exceptions.AuthenticatorException
import com.mohiva.play.silhouette.api.{ExpirableAuthenticator, Logger, LoginInfo, StorableAuthenticator}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticatorService.{ID, InvalidCookieSignature, InvalidJson, InvalidJsonFormat}
import org.joda.time.DateTime
import play.api.libs.json.{Format, JsNumber, JsResult, JsSuccess, JsValue, Json, OFormat}
import play.api.mvc.Cookie
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import reactivemongo.api.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONReader, BSONWriter, Macros}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/** A custom cookie authenticator.
 *
 * An authenticator that uses a stateful as well as stateless, cookie based approach.
 *
 * It works either by storing an ID in a cookie to track the authenticated user and a server side backing
 * store that maps the ID to an authenticator instance or by a stateless approach that stores the authenticator
 * in a serialized form directly into the cookie. The stateless approach could also be named “server side session”.
 *
 * The authenticator can use sliding window expiration. This means that the authenticator times
 * out after a certain time if it wasn't used. This can be controlled with the [[idleTimeout]]
 * property.
 *
 * With this authenticator it's possible to implement "Remember Me" functionality. This can be
 * achieved by updating the `expirationDateTime`, `idleTimeout` or `cookieMaxAge` properties of
 * this authenticator after it was created and before it gets initialized.
 *
 * Note: If deploying to multiple nodes the backing store will need to synchronize.
 *
 * @param id                 The authenticator ID.
 * @param loginInfo          The linked login info for an identity.
 * @param lastUsedDateTime   The last used date/time.
 * @param expirationDateTime The expiration date/time.
 * @param idleTimeout        The duration an authenticator can be idle before it timed out.
 * @param cookieMaxAge       The duration a cookie expires. `None` for a transient cookie.
 * @param fingerprint        Maybe a fingerprint of the user.
 * @param passedTwoFactor    The result of passing through second factor.
 */
case class ExtendedCookieAuthenticator(
                                        id: String,
                                        _id: BSONObjectID,
                                        loginInfo: LoginInfo,
                                        lastUsedDateTime: DateTime,
                                        expirationDateTime: DateTime,
                                        idleTimeout: Option[FiniteDuration],
                                        cookieMaxAge: Option[FiniteDuration],
                                        fingerprint: Option[String],
                                        passedTwoFactor: Boolean
                                      ) extends StorableAuthenticator with ExpirableAuthenticator {
  override type Value = Cookie
}

object ExtendedCookieAuthenticator extends Logger {


  /**
   * Converts the CookieAuthenticator to Json and vice versa.
   */
  implicit object FiniteDurationFormat extends Format[FiniteDuration] {
    def reads(json: JsValue): JsResult[FiniteDuration] = LongReads.reads(json).map(_.seconds)

    def writes(o: FiniteDuration): JsValue = LongWrites.writes(o.toSeconds)
  }

  //  implicit val dateTimeWriter1: BSONWriter[DateTime] =
  //    BSONWriter.from[DateTime] { score =>
  //      scala.util.Success(BSONDateTime(score.getMillis))
  //    }
  //
  //  implicit val dateTimeReader: BSONReader[DateTime] = BSONReader.from[DateTime] { bson =>
  //    bson.asTry[BSONDateTime].flatMap(dt => Try(new DateTime(dt.value)))
  //  }


  import reactivemongo.play.json.compat
  import compat.bson2json._
  import compat.json2bson._
  import play.api.libs.json.JodaWrites._
  import play.api.libs.json.JodaReads._

  //  implicit val dateTimeReader: BSONReader[DateTime] = BSONReader[DateTime]{
  //    case  t: BSONDateTime => new DateTime(t.value)
  //  }
  //
  //  implicit val dateTimeWriter: BSONWriter[DateTime] = BSONWriter[DateTime]{ time =>
  //    BSONDateTime(time.getMillis)
  //  }
  //
  //  implicit val authRolesFormat = new Format[DateTime] {
  //    def reads(json: JsValue) =  JsSuccess(new DateTime(json.as[BSONDateTime].value))
  //    def writes(authRole: DateTime) = BSONDateTime(authRole.getMillis).asOpt[JsValue].get
  //  }

  implicit val reader: BSONDocumentReader[ExtendedCookieAuthenticator] = Macros.reader[ExtendedCookieAuthenticator]
  implicit val jsonFormat: Format[ExtendedCookieAuthenticator] = Json.format[ExtendedCookieAuthenticator]
  implicit val format: OFormat[ExtendedCookieAuthenticator] = Json.format[ExtendedCookieAuthenticator] // for mongodb

  /**
   * Serializes the authenticator.
   *
   * @param authenticator        The authenticator to serialize.
   * @param signer               The signer implementation.
   * @param authenticatorEncoder The authenticator encoder.
   * @return The serialized authenticator.
   */
  def serialize(
                 authenticator: ExtendedCookieAuthenticator,
                 signer: Signer,
                 authenticatorEncoder: AuthenticatorEncoder) = {

    signer.sign(authenticatorEncoder.encode(Json.toJson(authenticator).toString()))
  }

  /**
   * Unserializes the authenticator.
   *
   * @param str                  The string representation of the authenticator.
   * @param signer               The signer implementation.
   * @param authenticatorEncoder The authenticator encoder.
   * @return Some authenticator on success, otherwise None.
   */
  def unserialize(
                   str: String,
                   signer: Signer,
                   authenticatorEncoder: AuthenticatorEncoder): Try[ExtendedCookieAuthenticator] = {

    signer.extract(str) match {
      case Success(data) => buildAuthenticator(authenticatorEncoder.decode(data))
      case Failure(e) => Failure(new AuthenticatorException(InvalidCookieSignature.format(ID), e))
    }
  }

  /**
   * Builds the authenticator from Json.
   *
   * @param str The string representation of the authenticator.
   * @return An authenticator on success, otherwise a failure.
   */
  private def buildAuthenticator(str: String): Try[ExtendedCookieAuthenticator] = {
    Try(Json.parse(str)) match {
      case Success(json) => json.validate[ExtendedCookieAuthenticator].asEither match {
        case Left(error) => Failure(new AuthenticatorException(InvalidJsonFormat.format(ID, error)))
        case Right(authenticator) => Success(authenticator)
      }
      case Failure(error) => Failure(new AuthenticatorException(InvalidJson.format(ID, str), error))
    }
  }
}
