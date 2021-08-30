package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * Give access to the userRegistration object.
 */
class UserDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends DefaultDAOImpl[User] {
  override protected def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = BSONDocument("email" -> loginInfo.providerKey, "credentialProviderId" -> loginInfo.providerID)
    super.find(query)
  }
}