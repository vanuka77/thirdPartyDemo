package controllers

import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.mvc.{AnyContent, Request}
import utils.auth.WithProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeCounterController @Inject()(
                                         scc: SilhouetteControllerComponents
                                       )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {
  def changeCounter() = SecuredAction(WithProvider[AuthType](CredentialsProvider.ID)).async { implicit request: Request[AnyContent] =>
    Future(Ok)
  }

}