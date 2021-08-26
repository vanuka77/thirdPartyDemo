package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.play.json.compat.json2bson.toDocumentWriter

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * Give access to the user object.
 */
class UserDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends DefaultDAOImpl[User] {
  override protected def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("users"))

  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = BSONDocument("email" -> loginInfo.providerKey, "credentialProviderId" -> loginInfo.providerID)
    super.find(query)
  }

  def save(user: User): Future[User] = super.insert(user)

}