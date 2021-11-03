package models.services

import com.mohiva.play.silhouette.api.services.IdentityService
import models.User
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.Future

/**
 * Handles database actions to collection of users.
 */
trait UserService extends IdentityService[User] {
  /** Inserts a user into the collection.
   *
   * @param user the model to insert.
   * @return an inserted model.
   * */
  def save(user: User): Future[User]

  /** Updates a user with the given id .
   *
   * @param id   an object to define a model in database.
   * @param user a new model.
   * @return an updated model from database.
   * */
  def update(id: BSONObjectID, user: User): Future[Option[User]]

  /** Returns user using hashId from secondFactor.
   *
   * @param hashId a hashId from secondFactor. */
  def retrieveByHashId(hashId: String): Future[Option[User]]

  /** Returns a model with the given id.
   *
   * @param id – an object to define a model in database. */
  def retrieveById(id: BSONObjectID): Future[Option[User]]

  /** Returns user using second factor id.
   *
   * @param twoFactorId second factor id . */
  def retrieveByTwoFactorId(twoFactorId: String): Future[Option[User]]


  /** Returns user using an email.
   *
   * @param email second factor id . */
  def retrieveByEmail(email: String): Future[Option[User]]
}
