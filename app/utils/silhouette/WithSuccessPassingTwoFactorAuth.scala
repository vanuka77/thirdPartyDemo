package utils.silhouette

import com.mohiva.play.silhouette.api.Authorization
import models.User
import play.api.mvc.Request
import utils.silhouette.authenticator.ExtendedCookieAuthenticator

import scala.concurrent.Future

/**
 * Authorization object that let you hook an authorization implementation in secured endpoints.
 * */
case class WithSuccessPassingTwoFactorAuth() extends Authorization[User, ExtendedCookieAuthenticator] {

  /**
   * Checks whether the user is authorized to execute an endpoint or not.
   *
   * @param user          The current identity instance.
   * @param authenticator The current authenticator instance.
   * @param r             The current request.
   * @tparam A The type of the request body.
   * @return True if the user is authorized and passed through second factor, false otherwise.
   */
  def isAuthorized[A](user: User, authenticator: ExtendedCookieAuthenticator)(implicit r: Request[A]) = Future.successful {
    authenticator.passedTwoFactor
  }
}
