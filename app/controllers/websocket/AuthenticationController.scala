package controllers.websocket

import actors.AuthenticationActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.HandlerResult
import controllers.auth.{SilhouetteController, SilhouetteControllerComponents}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AnyContentAsEmpty, Request, WebSocket}
import scala.concurrent.{ExecutionContext, Future}

/** A controller for websocket which handles communication with front end when user is trying to authenticate. */
class AuthenticationController @Inject()(
                                          scc: SilhouetteControllerComponents,
                                        )(implicit val system: ActorSystem, val materializer: Materializer, ex: ExecutionContext) extends SilhouetteController(scc) {
  /** Creates a websocket. */
  def socket() = WebSocket.acceptOrResult[String, String] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(_, Some(_)) => Right(ActorFlow.actorRef(AuthenticationActor.props()))
      case HandlerResult(r, None) => Left(r)
    }
  }
}
