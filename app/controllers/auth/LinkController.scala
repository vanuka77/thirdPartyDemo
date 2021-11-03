package controllers.auth

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import models.services.UserService
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.AnyContent
import play.twirl.api.Html
import utils.silhouette.request.SecondFactorRequestProcessor
import utils.silhouette.request.signature.SignatureInfosGenerator

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

/** A controller which handles requests which links or unlinks user with second authorization. */
@Singleton
class LinkController @Inject()(
                                scc: SilhouetteControllerComponents,
                                userService: UserService,
                                requestProcessor: SecondFactorRequestProcessor,
                                config: Configuration
                              )(implicit val system: ActorSystem, val materializer: Materializer, ex: ExecutionContext)
  extends SilhouetteController(scc) {
  /** Links user with second authorization */
  def link() = silhouette.SecuredAction { implicit request =>
    Ok(views.html.link(config))
  }

  /** Unlinks user with second authorization */
  def unlink() = silhouette.SecuredAction.async { implicit securedRequest: SecuredRequest[EnvType, AnyContent] =>
    securedRequest.identity.secondFactorLinkingData match {
      case Some(data) =>
        val baseUrl = config.get[String]("factor2.url.base")
        val unLinkUrl = config.get[String]("factor2.url.unlink")
        val url = baseUrl+unLinkUrl
        requestProcessor.process(
          url,
          null,
          "delete",
          "twoFactorId" -> data.twoFactorId.getOrElse("")).map {
          res =>
            logger.debug(s"Request to : $url Response: $res")
            println(s"Request to : $url Response: $res")
            if (res.status == 200) {
              val newUser = securedRequest.identity.copy(secondFactorLinkingData = None, isLinkedToSecondFactor = false)
              userService.update(newUser._id.get, newUser)
              Ok(views.html.myHome(isAuthenticated = true)
              (new Html("<h2>Unlinked successfully!</h2>")))
            } else {
              BadRequest(views.html.myHome(isAuthenticated = true)
              (new Html("<h2>Unlinking failed!</h2>")))
            }
        }

      case None => Future.successful(BadRequest("Invalid twoFactorLinkingData!"))
    }

  }

}