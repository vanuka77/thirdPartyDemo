package modules

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import utils.silhouette.authenticator.ExtendedCookieAuthenticator
import utils.silhouette.authenticator.repository.AuthenticatorRepositoryImpl
//import models.CookieAuthenticator
import models.daos._
import models.services.{UserService, UserServiceImpl}
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.Future
/** Using for binding in this application.*/
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[UserService].to[UserServiceImpl]
  }
}