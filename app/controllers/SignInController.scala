package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import play.api.i18n.Lang
import play.api.libs.json.{JsString, Json, OFormat}
import play.api.mvc.{AnyContent, Request}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign In` controller.
 */
class SignInController @Inject()(
                                  scc: SilhouetteControllerComponents
                                )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  case class SignInModel(email: String, password: String)

  implicit val signInFormat: OFormat[SignInModel] = Json.format[SignInModel]

  /**
   * Handles sign in request
   *
   * @return JWT token in header if login is successful or Bad request if credentials are invalid
   */
  def signIn() = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    implicit val lang: Lang = supportedLangs.availables.head
    request.body.asJson.flatMap(_.asOpt[SignInModel]) match {
      case Some(signInModel) =>
        val credentials = Credentials("ivan@123@gmail.com", "123123")
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          for {
                      authenticator <- silhouette.env.authenticatorService.create(loginInfo)
                      authToken <- silhouette.env.authenticatorService.init(authenticator)
                      res <- silhouette.env.authenticatorService.embed(authToken, Ok)
////                        authId<-cookie.value
//                        auth<-authInfoRepository.find(loginInfo)
//                        authenticator <- silhouette.env.authenticatorService.retrieve(request)
//                        authToken <- silhouette.env.authenticatorService.init(authenticator.get)
//            res <- silhouette.env.authenticatorService.embed(authToken, Ok)
          } yield res
        }.recover {
          case q: ProviderException => BadRequest(JsString(messagesApi(q.getMessage)))
        }
      case None => Future.successful(BadRequest(JsString(messagesApi("could.not.find.user"))))
    }
  }
}
