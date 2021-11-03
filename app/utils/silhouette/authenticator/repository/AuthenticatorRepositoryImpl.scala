package utils.silhouette.authenticator.repository

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import models.services.UserService
import utils.silhouette.authenticator.ExtendedCookieAuthenticator
import utils.silhouette.authenticator.dao.ExtendedCookieAuthenticatorDAOImpl

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/** Provides the means to persist authenticator information into database. */
class AuthenticatorRepositoryImpl @Inject()(val extendedCookieAuthenticatorDAO: ExtendedCookieAuthenticatorDAOImpl,
                                            val userService: UserService)
                                           (implicit ex: ExecutionContext)
  extends AuthenticatorRepository[ExtendedCookieAuthenticator] {

  /**
   * Finds the authenticator for the given id.
   *
   * @param id The authenticator ID.
   * @return The found authenticator or None if no authenticator could be found for the given ID.
   */
  override def find(id: String): Future[Option[ExtendedCookieAuthenticator]] = extendedCookieAuthenticatorDAO.find(id)

  /**
   * Adds a new authenticator.
   *
   * @param authenticator The authenticator to add.
   * @return The added authenticator.
   */
  override def add(authenticator: ExtendedCookieAuthenticator): Future[ExtendedCookieAuthenticator] = {
    extendedCookieAuthenticatorDAO.insert(authenticator)
  }

  /**
   * Updates an already existing authenticator.
   *
   * @param authenticator The authenticator to update.
   * @return The updated authenticator.
   */
  override def update(authenticator: ExtendedCookieAuthenticator): Future[ExtendedCookieAuthenticator] =
    extendedCookieAuthenticatorDAO.update(authenticator.id, authenticator).map(_ => authenticator)

  /**
   * Removes the authenticator for the given ID.
   *
   * @param id The authenticator ID.
   * @return An empty future.
   */
  override def remove(id: String): Future[Unit] = extendedCookieAuthenticatorDAO.remove(id).map(_ => ())
}
