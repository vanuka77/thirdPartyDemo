package controllers

import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import models.daos.UserDAO
import models.services.UserService
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign Up` controller.
 */
class SignUpController @Inject()(
                                  components: SilhouetteControllerComponents,
                                  userService: UserService
                                )(implicit ex: ExecutionContext) extends SilhouetteController(components) {

  //  implicit val userFormat: OFormat[IdentityType] = Json.format[User]

  /**
   * Handles sign up request
   *
   * @return The result to display.
   */
  def signUp() = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    for {
      user <- userService.save(User(Some(BSONObjectID.generate()), UUID.randomUUID(), CredentialsProvider.ID, "qwerqwe@123@gmail.com", "qweqweqwe", "qweqweqweqwe", Some("321321")))
      authenticator <- silhouette.env.authenticatorService.create(user.loginInfo)
      authToken <- silhouette.env.authenticatorService.init(authenticator)
      res <- silhouette.env.authenticatorService.embed(authToken, Ok)
    } yield res

  }


}
