package actors

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import models.chat.SecondFactorMessage
import models.json.WebSocketAction
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext

/** Factory for akka.actor.Props instances.
  * These instances are needed for [[controllers.websocket.AuthenticationController]].
  * */
object AuthenticationActor {

  /** Creates an instance of [[akka.actor.Props]] */
  def props()(out: ActorRef)(implicit
      system: ActorSystem,
      materializer: Materializer,
      executionContext: ExecutionContext
  ) =
    Props(new AuthenticationActor(out))
}

/** An actor which handles messages from [[controllers.websocket.AuthenticationController]] and system events. */
class AuthenticationActor(out: ActorRef)(implicit
    system: ActorSystem,
    materializer: Materializer,
    executionContext: ExecutionContext
) extends Actor {

  /** Subscribes to system events with [[models.chat.SecondFactorMessage]] channel type. */
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    context.system.eventStream
      .subscribe(context.self, classOf[SecondFactorMessage])
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
    case jsString: String
        if Json.parse(jsString).asOpt[WebSocketAction].isDefined =>
      val webSocketAction = Json.parse(jsString).as[WebSocketAction]
      webSocketAction.action.trim match {
        case "stop" => self ! PoisonPill
        case _ =>
          out ! Json
            .toJson(webSocketAction.copy(answer = Some("undefined action")))
            .toString()
      }

    case c: SecondFactorMessage =>
      c.operationType match {
        case "processLogIn" =>
          out ! Json.toJson(WebSocketAction(c.operationType)).toString()
        case "stop" => context.stop(self); self ! PoisonPill
        case _ =>
          out ! Json
            .toJson(WebSocketAction("error", Some("undefined action")))
            .toString()
      }
    case _ =>
      out ! Json
        .toJson(WebSocketAction("error", Some("undefined action")))
        .toString()
  }

}
