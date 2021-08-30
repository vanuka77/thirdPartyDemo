package models.daos.silhouette

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.daos.UserDAOImpl
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

//This class descibes dao actions for Auth info
//PasswordInfo is one of implementation of Auth info
class PasswordInfoImpl @Inject()(userDAO: UserDAOImpl)(implicit val classTag: ClassTag[PasswordInfo], ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo] {

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    userDAO.retrieve(loginInfo).map {
      case Some(user) =>
        Some(PasswordInfo(BCryptSha256PasswordHasher.ID, user.password.get))
      case None => None
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = update(loginInfo, authInfo)

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = userDAO
    .find(loginInfo.providerKey).flatMap {
    case Some(user) => userDAO.update(BSONObjectID.pretty(user._id.get), user.copy(password = Some(authInfo.password))).map(_.get.passwordInfo)
    case None => Future.failed(new Exception("user not found"))
  }

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = update(loginInfo, authInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] = update(loginInfo, PasswordInfo("", "")).map(_ => ())
}
