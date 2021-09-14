package controllers

import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.services.UserService
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import utils.auth.CookieEnv

//import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeCounterController @Inject()(
                                         scc: SilhouetteControllerComponents
                                       )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {
  var counter = 0

  def changeCounter() = silhouette.SecuredAction { implicit request: Request[AnyContent] =>
    counter += 1
    Ok(counter.toString)
  }

  //  def changeCounter = SecuredAction(WithProvider[AuthType](CredentialsProvider.ID)).async {
  //    implicit request: SecuredRequest[CookieAuthenticatorEnvironment, AnyContent] =>
  //      val credentials = Credentials("ivan@123@gmail.com", "123123")
  //      credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
  //        val passwordInfo = passwordHasherRegistry.current.hash("newPassword")
  //        authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
  //          Ok
  //
  //        }
  //      }.recover {
  //        case _: ProviderException =>
  //          Ok
  //
  //      }
  //  }

}