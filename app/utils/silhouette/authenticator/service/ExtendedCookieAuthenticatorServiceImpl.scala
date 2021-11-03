package utils.silhouette.authenticator.service

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.crypto.{AuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.exceptions._
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService._
import com.mohiva.play.silhouette.api.services.{AuthenticatorResult, AuthenticatorService}
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticatorService._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticatorSettings
import play.api.mvc._
import play.api.mvc.request.{Cell, RequestAttrKey}
import reactivemongo.api.bson.BSONObjectID
import utils.silhouette.authenticator.ExtendedCookieAuthenticator

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * Handles authenticators for the Silhouette module.
 */
class ExtendedCookieAuthenticatorServiceImpl(
                                              settings: CookieAuthenticatorSettings,
                                              repository: Option[AuthenticatorRepository[ExtendedCookieAuthenticator]],
                                              signer: Signer,
                                              cookieHeaderEncoding: CookieHeaderEncoding,
                                              authenticatorEncoder: AuthenticatorEncoder,
                                              fingerprintGenerator: FingerprintGenerator,
                                              idGenerator: IDGenerator,
                                              clock: Clock
                                            )(
                                              implicit
                                              val executionContext: ExecutionContext
                                            ) extends AuthenticatorService[ExtendedCookieAuthenticator] with Logger {

  import ExtendedCookieAuthenticator._

  /**
   * Creates a new authenticator for the specified login info.
   *
   * @param loginInfo The login info for which the authenticator should be created.
   * @param request   The request header.
   * @return An authenticator.
   */
  override def create(loginInfo: LoginInfo)(implicit request: RequestHeader): Future[ExtendedCookieAuthenticator] = {
    idGenerator.generate.map { id =>
      val now = clock.now
      ExtendedCookieAuthenticator(
        _id = BSONObjectID.generate(),
        id = id,
        loginInfo = loginInfo,
        lastUsedDateTime = now,
        expirationDateTime = now + settings.authenticatorExpiry,
        idleTimeout = settings.authenticatorIdleTimeout,
        cookieMaxAge = settings.cookieMaxAge,
        fingerprint = if (settings.useFingerprinting) Some(fingerprintGenerator.generate) else None,
        passedTwoFactor = false,
      )
    }.recover {
      case e => throw new AuthenticatorCreationException(CreateError.format(ID, loginInfo), e)
    }
  }

  /**
   * Retrieves the authenticator from request.
   *
   * @param request The request to retrieve the authenticator from.
   * @tparam B The type of the request body.
   * @return Some authenticator or None if no authenticator could be found in request.
   */
  override def retrieve[B](implicit request: ExtractableRequest[B]): Future[Option[ExtendedCookieAuthenticator]] = {
    Future.fromTry(Try {
      if (settings.useFingerprinting) Some(fingerprintGenerator.generate) else None
    }).flatMap { fingerprint =>
      request.cookies.get(settings.cookieName) match {
        case Some(cookie) =>
          (repository match {
            case Some(d) => d.find(cookie.value)
            case None => unserialize(cookie.value, signer, authenticatorEncoder) match {
              case Success(authenticator) => Future.successful(Some(authenticator))
              case Failure(error) =>
                logger.info(error.getMessage, error)
                Future.successful(None)
            }
          }).map {
            case Some(a) if fingerprint.isDefined && a.fingerprint != fingerprint =>
              logger.info(InvalidFingerprint.format(ID, fingerprint, a))
              None
            case v => v
          }
        case None => Future.successful(None)
      }
    }.recover {
      case e => throw new AuthenticatorRetrievalException(RetrieveError.format(ID), e)
    }
  }

  /**
   * Creates a new cookie for the given authenticator and return it.
   *
   * If the stateful approach will be used the the authenticator will also be
   * stored in the backing store.
   *
   * @param authenticator The authenticator instance.
   * @param request       The request header.
   * @return The serialized authenticator value.
   */
  override def init(authenticator: ExtendedCookieAuthenticator)(implicit request: RequestHeader): Future[Cookie] = {
    (repository match {
      case Some(d) => d.add(authenticator).map(_.id)
      case None => Future.successful(serialize(authenticator, signer, authenticatorEncoder))
    }).map { value =>
      Cookie(
        name = settings.cookieName,
        value = value,
        // The maxAge` must be used from the authenticator, because it might be changed by the user
        // to implement "Remember Me" functionality
        maxAge = authenticator.cookieMaxAge.map(_.toSeconds.toInt),
        path = settings.cookiePath,
        domain = settings.cookieDomain,
        secure = settings.secureCookie,
        httpOnly = settings.httpOnlyCookie,
        sameSite = settings.sameSite
      )
    }.recover {
      case e => throw new AuthenticatorInitializationException(InitError.format(ID, authenticator), e)
    }
  }

  /**
   * Embeds the cookie into the result.
   *
   * @param cookie  The cookie to embed.
   * @param result  The result to manipulate.
   * @param request The request header.
   * @return The manipulated result.
   */
  override def embed(cookie: Cookie, result: Result)(implicit request: RequestHeader): Future[AuthenticatorResult] = {
    Future.successful(AuthenticatorResult(result.withCookies(cookie)))
  }

  /**
   * Embeds the cookie into the request.
   *
   * @param cookie  The cookie to embed.
   * @param request The request header.
   * @return The manipulated request header.
   */
  override def embed(cookie: Cookie, request: RequestHeader): RequestHeader = {
    val filteredCookies = request.cookies.filter(_.name != cookie.name).toSeq
    val combinedCookies = filteredCookies :+ cookie
    val cookies = Cookies(combinedCookies)

    request.withAttrs(request.attrs + RequestAttrKey.Cookies.bindValue(Cell(cookies)))
  }

  /**
   * @param authenticator The authenticator to touch.
   * @return The touched authenticator on the left or the untouched authenticator on the right.
   */
  override def touch(authenticator: ExtendedCookieAuthenticator): Either[ExtendedCookieAuthenticator,
    ExtendedCookieAuthenticator] = {
    if (authenticator.idleTimeout.isDefined) {
      Left(authenticator.copy(lastUsedDateTime = clock.now))
    } else {
      Right(authenticator)
    }
  }

  /**
   * Updates the authenticator with the new last used date.
   *
   * If the stateless approach will be used then we update the cookie on the client. With the stateful approach
   * we needn't embed the cookie in the response here because the cookie itself will not be changed. Only the
   * authenticator in the backing store will be changed.
   *
   * @param authenticator The authenticator to update.
   * @param result        The result to manipulate.
   * @param request       The request header.
   * @return The original or a manipulated result.
   */
  override def update(authenticator: ExtendedCookieAuthenticator, result: Result)(
    implicit
    request: RequestHeader): Future[AuthenticatorResult] = {

    (repository match {
      case Some(d) => d.update(authenticator).map(_ => AuthenticatorResult(result))
      case None => Future.successful(AuthenticatorResult(result.withCookies(Cookie(
        name = settings.cookieName,
        value = serialize(authenticator, signer, authenticatorEncoder),
        // The maxAge` must be used from the authenticator, because it might be changed by the user
        // to implement "Remember Me" functionality
        maxAge = authenticator.cookieMaxAge.map(_.toSeconds.toInt),
        path = settings.cookiePath,
        domain = settings.cookieDomain,
        secure = settings.secureCookie,
        httpOnly = settings.httpOnlyCookie,
        sameSite = settings.sameSite
      ))))
    }).recover {
      case e => throw new AuthenticatorUpdateException(UpdateError.format(ID, authenticator), e)
    }
  }

  /**
   * Renews an authenticator.
   *
   * After that it isn't possible to use a cookie which was bound to this authenticator. This method
   * doesn't embed the the authenticator into the result. This must be done manually if needed
   * or use the other renew method otherwise.
   *
   * @param authenticator The authenticator to renew.
   * @param request       The request header.
   * @return The serialized expression of the authenticator.
   */
  override def renew(authenticator: ExtendedCookieAuthenticator)(implicit request: RequestHeader): Future[Cookie] = {
    (repository match {
      case Some(d) => d.remove(authenticator.id)
      case None => Future.successful(())
    }).flatMap { _ =>
      create(authenticator.loginInfo).flatMap(init)
    }.recover {
      case e => throw new AuthenticatorRenewalException(RenewError.format(ID, authenticator), e)
    }
  }

  /**
   * Renews an authenticator and replaces the authenticator cookie with a new one.
   *
   * If the stateful approach will be used then the old authenticator will be revoked in the backing
   * store. After that it isn't possible to use a cookie which was bound to this authenticator.
   *
   * @param authenticator The authenticator to update.
   * @param result        The result to manipulate.
   * @param request       The request header.
   * @return The original or a manipulated result.
   */
  override def renew(authenticator: ExtendedCookieAuthenticator, result: Result)(
    implicit
    request: RequestHeader): Future[AuthenticatorResult] = {

    renew(authenticator).flatMap(v => embed(v, result)).recover {
      case e => throw new AuthenticatorRenewalException(RenewError.format(ID, authenticator), e)
    }
  }

  /**
   * Discards the cookie.
   *
   * If the stateful approach will be used then the authenticator will also be removed from backing store.
   *
   * @param result  The result to manipulate.
   * @param request The request header.
   * @return The manipulated result.
   */
  override def discard(authenticator: ExtendedCookieAuthenticator, result: Result)(
    implicit
    request: RequestHeader): Future[AuthenticatorResult] = {

    (repository match {
      case Some(d) => d.remove(authenticator.id)
      case None => Future.successful(())
    }).map { _ =>
      AuthenticatorResult(result.discardingCookies(DiscardingCookie(
        name = settings.cookieName,
        path = settings.cookiePath,
        domain = settings.cookieDomain,
        secure = settings.secureCookie)))
    }.recover {
      case e => throw new AuthenticatorDiscardingException(DiscardError.format(ID, authenticator), e)
    }
  }
}

/**
 * The companion object of the authenticator service.
 */
object ExtendedCookieAuthenticatorServiceImpl {

  /**
   * The ID of the authenticator.
   */
  val ID = "cookie-authenticator"

  /**
   * The error messages.
   */
  val InvalidJson = "[Silhouette][%s] Cannot parse invalid Json: %s"
  val InvalidJsonFormat = "[Silhouette][%s] Invalid Json format: %s"
  val InvalidFingerprint = "[Silhouette][%s] Fingerprint %s doesn't match authenticator: %s"
  val InvalidCookieSignature = "[Silhouette][%s] Invalid cookie signature"
}
