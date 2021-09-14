package models.services

import java.util.UUID
import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User
import models.daos.UserDAOImpl
import reactivemongo.api.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

/**
 * Handles actions to users.
 *
 * @param userDAO The userRegistration DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject()(userDAO: UserDAOImpl)(implicit ex: ExecutionContext) extends UserService {


  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    userDAO.retrieve(loginInfo)
  }

  def save(user: User) = userDAO.insert(user)
}