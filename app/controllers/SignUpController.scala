package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import models.daos.UserDAO
import models.services.UserService
import play.api.cache.AsyncCacheApi
import play.api.i18n.Lang
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign Up` controller.
 */
class SignUpController @Inject()(
                                  components: SilhouetteControllerComponents,
                                  userService: UserService
                                )(implicit ex: ExecutionContext) extends SilhouetteController(components) {

  //  implicit val userFormat: OFormat[IdentityType] = Json.format[User]

  /**
   * Handles sign up request
   *
   * @return The result to display.
   */
  def signUp() = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    //    request.cookies.get("").
    val user = User(Some(BSONObjectID.generate()), LoginInfo(CredentialsProvider.ID, "qwe@123@gmail.com"), CredentialsProvider.ID, "ivan@123@gmail.com", "Ivan", "Lytvynenko", Some("123123"))
    val authInfo = passwordHasherRegistry.current.hash(user.password.get)
    val newUser = user.copy(password = Some(authInfo.password))
    userService.save(newUser).map(u => Ok(Json.toJson(u.copy(password = None))))
//    for {
//      user <- userService.save()
//      //      authenticator <- silhouette.env.authenticatorService.create(user.loginInfo)
//      //      authToken <- silhouette.env.authenticatorService.init(authenticator)
//      //      res <- silhouette.env.authenticatorService.embed(authToken, Ok)
//    } yield Ok
  }
//
//  }

//def signUp = UnsecuredAction.async { implicit request: Request[AnyContent] =>
//  implicit val lang: Lang = supportedLangs.availables.head
//  request.body.asJson.flatMap(_.asOpt[User]) match {
//    case Some(newUser) if newUser.password.isDefined =>
//      userService.retrieve(LoginInfo(CredentialsProvider.ID, newUser.email)).flatMap {
//        case Some(_) =>
//          Future.successful(Conflict(JsString(messagesApi("user.already.exist"))))
//        case None =>
//          val authInfo = passwordHasherRegistry.current.hash(newUser.password.get)
//          val user = newUser.copy(password = Some(authInfo.password))
//          userService.save(user).map(u => Ok(Json.toJson(u.copy(password = None))))
//      }
//    case _ => Future.successful(BadRequest(JsString(messagesApi("invalid.body"))))
//  }
//}


}
