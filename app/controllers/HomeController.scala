package controllers

import com.google.inject.Inject
import controllers.auth.{SilhouetteController, SilhouetteControllerComponents}
import play.twirl.api.Html

/** A controller which handles home request. */
class HomeController @Inject()(scc: SilhouetteControllerComponents) extends SilhouetteController(scc) {

  /** Returns home page. */
  def home(content: Html = new Html("")) = silhouette.UserAwareAction { implicit request =>
    request.identity match {
      case Some(identity) => Ok(views.html.myHome(isAuthenticated = true, identity.isLinkedToSecondFactor)(content))
      case None => Ok(views.html.myHome()(content))
    }
  }

}
