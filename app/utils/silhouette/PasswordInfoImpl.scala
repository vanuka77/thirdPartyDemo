package utils.silhouette

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.daos.UserDAOImpl
import reactivemongo.api.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/** The DAO to persist the auth info. */
class PasswordInfoImpl @Inject()(userDAO: UserDAOImpl)
                                (implicit val classTag: ClassTag[PasswordInfo], ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo] {

  /**
   * Finds the auth info which is linked to the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The found auth info or None if no auth info could be found for the given login info.
   */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    userDAO.retrieve(loginInfo).map {
      case Some(user) =>
        Some(PasswordInfo(BCryptSha256PasswordHasher.ID, user.password.get))
      case None => None
    }
  }
  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo  The auth info to add.
   * @return The added auth info.
   */
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = update(loginInfo, authInfo)
  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo  The auth info to update.
   * @return The updated auth info.
   */
  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    userDAO.find(BSONDocument("email" -> loginInfo.providerKey)).flatMap {
      case Some(u) => userDAO.update(u._id.get, u.copy(password = Some(authInfo.password))).map(_.get.passwordInfo)
      case None => Future.failed(new Exception("user not found"))
    }
  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo  The auth info to save.
   * @return The saved auth info.
   */
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = update(loginInfo, authInfo)
  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  override def remove(loginInfo: LoginInfo): Future[Unit] = update(loginInfo, PasswordInfo("", "")).map(_ => ())
}