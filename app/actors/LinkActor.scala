package actors

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import models.User
import models.chat.SecondFactorMessage
import models.json.WebSocketAction
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext

/** Factory for [[akka.actor.Props]] instances.
 * These instances are needed for [[controllers.websocket.LinkController]].
 * */
object LinkActor {
  /** Creates an instance of [[akka.actor.Props]]  with a given user
   *
   * @param user given User
   * @see [[models.User]]
   */
  def props(user: User)(out: ActorRef)
           (implicit system: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) =
    Props(new LinkActor(user, out))
}

/** An actor which handles messages from [[controllers.websocket.LinkController]] and system events.
 *
 * @param user given User
 * @see [[models.User]]
 */
class LinkActor(user: User, out: ActorRef)
               (implicit system: ActorSystem, materializer: Materializer, executionContext: ExecutionContext)
  extends Actor {

  /** Subscribes to system events with [[models.chat.SecondFactorMessage]] channel type. */
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    context.system.eventStream.subscribe(context.self, classOf[SecondFactorMessage])
    super.preStart()
  }

  /** Unsubscribes to system events with [[models.chat.SecondFactorMessage]] channel type. */
  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(context.self)
    super.postStop()
  }

  /** Describes behavior for this actor to incoming messages.
   *
   * @see [[models.json.WebSocketAction]]
   */
  def receive = {
    case jsString: String if Json.parse(jsString).asOpt[WebSocketAction].isDefined =>
      val webSocketAction = Json.parse(jsString).as[WebSocketAction]
      webSocketAction.action.trim match {
        case "showHashId" =>
          user.secondFactorLinkingData match {
            case Some(data) => out ! Json.toJson(
              WebSocketAction(
                webSocketAction.action,
                Some(data.hashId)
              )
            ).toString()
            case None => out ! Json.toJson(
              WebSocketAction(
                webSocketAction.action,
                Some("Problems with TwoFactorData")
              )
            ).toString()
          }

        case "stop" => self ! PoisonPill
        case _ => out ! Json.toJson(webSocketAction.copy(answer = Some("undefined action"))).toString()
      }

    case c: SecondFactorMessage => c.operationType
    match {
      case "processLink" => out ! Json.toJson(WebSocketAction(c.operationType)).toString()
      case "stop" => context.stop(self); self ! PoisonPill
      case _ => out ! Json.toJson(WebSocketAction("error", Some("undefined action"))).toString()
    }
    case _ => out ! Json.toJson(WebSocketAction("error", Some("undefined action"))).toString()
  }

}