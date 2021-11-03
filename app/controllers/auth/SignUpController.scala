package controllers.auth

import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import models.forms.UserSignUpForm.signUpForm
import models.services.UserService
import play.api.Configuration
import play.api.mvc._
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

/** A controller which handles authorization requests. */
@Singleton
class SignUpController @Inject()(
                                  components: SilhouetteControllerComponents,
                                  userService: UserService,
                                  config: Configuration
                                )(implicit ex: ExecutionContext) extends SilhouetteController(components) {
  /** Returns a form for signing up. */
  def getForm(errors: Option[String] = None) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.userSignUp(signUpForm, errors))
  }

  /** Handles a form for signing up. */
  def processForm() = Action.async { implicit request: Request[AnyContent] =>
    signUpForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.userSignUp(formWithErrors)))
      },
      userData => {
        userService.retrieveByEmail(userData.email) flatMap {
          case _ => Future.successful(BadRequest(views.html.userSignUp(signUpForm, Some(s"Email [${userData.email}] is already in use!"))))
          case None => val newUser = User(
            _id = Some(BSONObjectID.generate()),
            credentialProviderId = CredentialsProvider.ID,
            email = userData.email,
            name = userData.name,
            lastName = userData.lastName,
            password = Some(passwordHasherRegistry.current.hash(userData.password).password),
            None,
            isLinkedToSecondFactor = false,
            secondFactorLinkingData = None
          )
            userService.save(newUser)
            Future.successful(Redirect(controllers.auth.routes.AuthenticationController.getSignInForm()))
        }
      }
    )
  }
}
