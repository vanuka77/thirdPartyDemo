package controllers

import com.google.inject.Inject
import play.api.mvc.{AnyContent, Request}

import scala.concurrent.ExecutionContext

class WebHookLinking @Inject()(
                                components: SilhouetteControllerComponents,
                              )(implicit ex: ExecutionContext) extends SilhouetteController(components) {

  def link() = silhouette.UnsecuredAction { implicit request: Request[AnyContent] =>
    println(request.body.asJson)
    Ok
  }

  def success() = silhouette.UnsecuredAction { implicit request: Request[AnyContent] =>
    println(request.body.asJson)
    Ok
  }

  def fraud() = silhouette.UnsecuredAction { implicit request: Request[AnyContent] =>
    println(request.body.asJson)
    Ok
  }
}
