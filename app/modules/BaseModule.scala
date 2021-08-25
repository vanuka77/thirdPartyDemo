package modules

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
//import models.CookieAuthenticator
import models.daos._
import models.services.{UserService, UserServiceImpl}
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.Future

class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
//    bind[UserDAO].to[UserDAO]
    bind[UserService].to[UserServiceImpl]
    bind[AuthenticatorRepository[CookieAuthenticator]].to[AuthenticatorRepImpl]
  }
}