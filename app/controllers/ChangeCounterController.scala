package controllers

import com.google.inject.{Inject, Singleton}
import controllers.auth.{SilhouetteController, SilhouetteControllerComponents}
import play.api.mvc.{AnyContent, Request}
import utils.silhouette.WithSuccessPassingTwoFactorAuth

import scala.concurrent.ExecutionContext

/** A controller which handles request passed second factor. */
@Singleton
class ChangeCounterController @Inject()(
                                         scc: SilhouetteControllerComponents
                                       )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {
  var counter = 0

  /** Increments the counter. */
  def changeCounter() = silhouette.SecuredAction(WithSuccessPassingTwoFactorAuth()) { implicit request: Request[AnyContent] =>
    counter += 1
    Ok(views.html.counter(counter))
  }

}