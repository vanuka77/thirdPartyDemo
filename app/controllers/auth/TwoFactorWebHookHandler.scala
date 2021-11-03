package controllers.auth

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.warrenstrange.googleauth.GoogleAuthenticator
import models.chat.SecondFactorMessage
import models.services.UserService
import models.{SecondFactorLinkingData, SecondFactorLoginData}
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContent, Request}
import utils.silhouette.authenticator.repository.AuthenticatorRepositoryImpl
import utils.silhouette.request.validator.RequestValidator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** Controller which handles webhooks from second factor service. */
@Singleton
class TwoFactorWebHookHandler @Inject()(system: ActorSystem,
                                        components: SilhouetteControllerComponents,
                                        userService: UserService,
                                        authenticatorRepository: AuthenticatorRepositoryImpl,
                                        requestValidator: RequestValidator
                                       )(implicit materializer: Materializer, executionContext: ExecutionContext) extends SilhouetteController(components) {

  /** Handles link webhook . */
  def link() = Action.async(parse.json) { implicit request: Request[JsValue] =>
    val linkingData = request.body.as[SecondFactorLinkingData]
    userService.retrieveByHashId(linkingData.hashId).map {
      case Some(user) => {
        if (requestValidator.validate(request)) {
          val newUser = user.copy(secondFactorLinkingData = Some(linkingData), isLinkedToSecondFactor = true)
          userService.update(newUser._id.get, newUser)
          system.eventStream.publish(SecondFactorMessage("processLink"))
          println(s"Webhook \"/link\", incoming request : $request \nResponse: $Ok")
          Ok
        } else {
          println(s"Webhook \"/success\", incoming request : $request \nResponse: incorrect signature!!")
          Forbidden
        }
      }
      case None => {
        val response = BadRequest("Cannot find user!")
        println(s"Webhook \"/success\", incoming request : $request \nResponse: $response")
        response
      }
    }
  }

  /** Handles success login webhook . */
  def success() = Action.async(parse.json) { implicit request: Request[JsValue] =>
    val loginData = request.body.as[SecondFactorLoginData]
    userService.retrieveByTwoFactorId(loginData.twoFactorId).flatMap {
      case Some(user) => {
        if (requestValidator.validate(request)) {
          authenticatorRepository.find(user.authenticatorId.get).flatMap {
            case Some(authenticator) => {
              val resultAuthorizing = new GoogleAuthenticator().authorize(user.secondFactorLinkingData.get.secretKey.get, loginData.totpPassword)
              val newAuthenticator = authenticator.copy(passedTwoFactor = resultAuthorizing)
              authenticatorRepository.update(newAuthenticator)
              system.eventStream.publish(SecondFactorMessage("processLogIn"))
              println(s"Webhook \"/success\", incoming request : $request \nResponse: $Ok")
              Future.successful(Ok)
            }
            case None =>
              val response = BadRequest("Cannot find authenticator!")
              println(s"Webhook \"/success\", incoming request : $request \nResponse: $response")
              Future.successful(response)
          }
        } else {
          val response = Forbidden("Incorrect signature!!")
          println(s"Webhook \"/success\", incoming request : $request \nResponse: $response")
          Future.successful(response)
        }
      }
      case None => {
        val response = BadRequest("Cannot find user!")
        println(s"Webhook \"/success\", incoming request : $request \nResponse: $response")
        Future.successful(response)
      }
    }
  }

  /** Handles fraud webhook . */
  def fraud() = Action {
    implicit request: Request[AnyContent] =>
      Ok
  }
}