package controllers.websocket

import actors.LinkActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.HandlerResult
import controllers.auth.{SilhouetteController, SilhouetteControllerComponents}
import models.SecondFactorLinkingData
import play.api.Configuration
import play.api.libs.streams.ActorFlow
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{AnyContentAsEmpty, Request, WebSocket}
import utils.silhouette.request.SecondFactorRequestProcessor
import utils.silhouette.request.signature.SignatureInfosGenerator

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

/** A controller for websocket which handles communication with front end when user is trying to link with second factor. */
class LinkController @Inject()(
                                scc: SilhouetteControllerComponents,
                                requestProcessor: SecondFactorRequestProcessor,
                                config: Configuration
                              )(implicit val system: ActorSystem, val materializer: Materializer, ex: ExecutionContext) extends SilhouetteController(scc) {
  /** Opens websocket and starts process of linking user to second factor. */
  def socket() = WebSocket.acceptOrResult[String, String] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.flatMap {
      case HandlerResult(r, Some(user)) =>
        val baseUrl = config.get[String]("factor2.url.base")
        val linkUrl = config.get[String]("factor2.url.link")
        val url = baseUrl+linkUrl
        requestProcessor.process(url).flatMap {
          response => {
            logger.debug(s"Request to  : $url Response: $response")
            if (response.json("hashId").asOpt[String].isDefined) {
              val hashIdValue = response.json("hashId").as[String]
              val newTwoFactorLinkingData = SecondFactorLinkingData(
                hashId = hashIdValue,
                twoFactorId = None,
                secretKey = None)
              userService.update(user._id.get, user.copy(secondFactorLinkingData = Some(newTwoFactorLinkingData))).map {
                case Some(u) => Right(ActorFlow.actorRef(LinkActor.props(u)))
                case None => Right(ActorFlow.actorRef(LinkActor.props(user)))
              }
            } else {
              Future.successful(Right(ActorFlow.actorRef(LinkActor.props(user))))
            }
          }
        }

      case HandlerResult(r, None) => Future.successful(Left(r))
    }
  }
}
