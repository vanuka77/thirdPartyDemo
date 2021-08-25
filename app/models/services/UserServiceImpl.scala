package models.services

import java.util.UUID
import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User
import models.daos.UserDAO

import scala.concurrent.{ExecutionContext, Future}

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject()(userDAO: UserDAO)(implicit ex: ExecutionContext) extends UserService {

  def retrieve(id: String): Future[Option[User]] = userDAO.findById(id)

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  def save(user: User) = userDAO.insert(user)
}