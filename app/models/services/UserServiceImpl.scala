package models.services

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import models.daos.UserDAOImpl
import reactivemongo.api.bson.{BSONDocument, BSONObjectID}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/** Handles database actions to collection of users. */
class UserServiceImpl @Inject()(userDAO: UserDAOImpl)(implicit ex: ExecutionContext) extends UserService {
  /**
   * Retrieves an identity that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve an identity.
   * @return The retrieved identity or None if no identity could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.retrieve(loginInfo)

  /** Returns user using hashId from secondFactor.
   *
   * @param hashId a hashId from secondFactor. */
  def retrieveByHashId(hashId: String): Future[Option[User]] = userDAO.retrieveByHashId(hashId)

  /** Returns user using second factor id.
   *
   * @param twoFactorId second factor id . */
  def retrieveByTwoFactorId(twoFactorId: String): Future[Option[User]] = userDAO.retrieveByTwoFactorId(twoFactorId)

  /** Returns a model with the given id.
   *
   * @param id – an object to define a model in database. */
  def retrieveById(id: BSONObjectID): Future[Option[User]] = userDAO.find(id)

  /** Inserts a user into the collection.
   *
   * @param user the model to insert.
   * @return an inserted model.
   * */
  def save(user: User): Future[User] = userDAO.insert(user)

  /** Updates a user with the given id .
   *
   * @param id   an object to define a model in database.
   * @param user a new model.
   * @return an updated model from database.
   * */
  def update(id: BSONObjectID, user: User): Future[Option[User]] = userDAO.update(id, user)

  /** Returns user using an email.
   *
   * @param email second factor id . */
  def retrieveByEmail(email: String): Future[Option[User]] = userDAO.retrieveByEmail(email)
}