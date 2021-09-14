package controllers

import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import models.forms.UserRegistrationForm.form
import models.services.UserService
import play.api.libs.json.Json
import play.api.mvc._
import reactivemongo.api.bson.BSONObjectID

//import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign Up` controller.
 */
@Singleton
class SignUpController @Inject()(
                                  components: SilhouetteControllerComponents,
                                  userService: UserService
                                )(implicit ex: ExecutionContext) extends SilhouetteController(components) {

  def indexForm() = silhouette.UnsecuredAction { implicit request: Request[AnyContent] =>
    Ok(views.html.user(form))
  }

  /**
   * Handles sign up request
   *
   * @return The result to display.
   */
  def process() = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.user(formWithErrors)))
      },
      userData => {
        val newUser = User(
          Some(BSONObjectID.generate()),
          CredentialsProvider.ID,
          userData.email,
          userData.name,
          userData.lastName,
          Some(passwordHasherRegistry.current.hash(userData.password).password)
        )
//        println(userData.email)
        userService.save(newUser).map(u => Ok(Json.toJson(u.copy(password = None))))
      }
    )
  }
}
