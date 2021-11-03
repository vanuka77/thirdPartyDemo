package controllers.auth

import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.util.Credentials
import models.forms.UserSignInForm.signInForm
import models.services.UserService
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{AnyContent, Request}
import utils.silhouette.request.SecondFactorRequestProcessor
import utils.silhouette.request.signature.SignatureInfosGenerator

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

/** A controller which handles authentication requests. */
@Singleton
class AuthenticationController @Inject()(
                                          scc: SilhouetteControllerComponents,
                                          userService: UserService,
                                          requestProcessor: SecondFactorRequestProcessor,
                                          config: Configuration
                                        )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /** Returns a signing in form. */
  def getSignInForm() = silhouette.UnsecuredAction { implicit request: Request[AnyContent] =>
    Ok(views.html.userSignIn(signInForm))
  }

  /** Handles a signing in form. */
  def processSignInForm() = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    signInForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.userSignIn(formWithErrors)))
      },
      userData => {
        val credentials = Credentials(userData.email, userData.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>

          val responseInfo = for {
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            authToken <- silhouette.env.authenticatorService.init(authenticator)
            user <- userService.retrieve(loginInfo)
            newUser <- Future.successful(user.get.copy(authenticatorId = Some(authenticator.id)))
            updatedUser <- userService.update(newUser._id.get, newUser)
          } yield (updatedUser, authToken)

          responseInfo.flatMap(info => {
            if (info._1.get.isLinkedToSecondFactor) {
              silhouette.env.authenticatorService.embed(info._2, Redirect(controllers.auth.routes.AuthenticationController.processTwoFactor()))
            } else {
              silhouette.env.authenticatorService.embed(info._2, Redirect(controllers.auth.routes.LinkController.link()))
            }
          })
        }.recover(i => {
          BadRequest("Invalid credentials!")
        })
      })
  }

  /** Starts a second factor. */
  def processTwoFactor() = silhouette.SecuredAction.async { implicit securedRequest: SecuredRequest[EnvType, AnyContent] =>
    securedRequest.identity.secondFactorLinkingData match {
      case Some(data) =>
        val requestData = Json.obj("twoFactorId" -> data.twoFactorId)
        val baseUrl = config.get[String]("factor2.url.base")
        val startTwoFactorUrl = config.get[String]("factor2.url.startTwoFactor")
        val url = baseUrl+startTwoFactorUrl
        requestProcessor.process(url, requestData).map {
          res =>
            logger.debug(s"Request to: $url Response: $res")
            if (res.status == 200) {
              Ok(views.html.twoFactor(config))
            } else {
              BadRequest("Incorrect twoFactorId!")
            }
        }
      case None => Future.successful(BadRequest("Don't have TwoFactorLinkingData!"))
    }
  }

  /** Sign out a user. */
  def signOut() = silhouette.SecuredAction.async { implicit request =>
    authenticatorService.discard(request.authenticator, Redirect(controllers.auth.routes.AuthenticationController.getSignInForm())).recover(
      _ => BadRequest("Problems with deleting authenticator!"))
  }
}