package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import models.daos.UserDAO
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, userDao: UserDAO,cacheApi:AsyncCacheApi) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    userDao.insert(User(Some(BSONObjectID.generate()), LoginInfo(CredentialsProvider.ID,"ivan@123@gmail.com"),CredentialsProvider.ID, "ivan@123@gmail.com", "Ivan", "Lytvynenko", Some("123123")))
    println(request.cookies.get("authenticator"))
    Ok(views.html.index())
  }
}