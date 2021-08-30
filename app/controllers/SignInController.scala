package controllers

import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import models.forms.UserLoginForm.form
import play.api.i18n.Lang
import play.api.libs.json.JsString
import play.api.mvc.{AnyContent, Request}

//import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign In` controller.
 */
@Singleton
class SignInController @Inject()(
                                  scc: SilhouetteControllerComponents
                                )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {



  def indexForm() = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.userLogin(form)))
  }
  /**
   * Handles sign in request
   *
   * @return JWT token in header if login is successful or Bad request if credentials are invalid
   */
  def process() = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.userLogin(formWithErrors)))
      },
      userData => {
        val credentials = Credentials(userData.email, userData.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          for {
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            authToken <- silhouette.env.authenticatorService.init(authenticator)
            res <- silhouette.env.authenticatorService.embed(authToken, Ok("Login success!"))
          } yield res
        }.recover {
          case p: ProviderException => {
            implicit val lang: Lang = supportedLangs.availables.head
            BadRequest(JsString(messagesApi(p.getMessage)))
          }
        }
      }
    )
  }
}
