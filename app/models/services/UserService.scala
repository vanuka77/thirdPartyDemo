package models.services

import com.mohiva.play.silhouette.api.LoginInfo

import java.util.UUID
import com.mohiva.play.silhouette.api.services.IdentityService
import models.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {
  def save(user: User): Future[User]
}