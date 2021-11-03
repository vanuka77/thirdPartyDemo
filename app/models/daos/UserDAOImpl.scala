package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.bson.collection.BSONCollection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * DAO for user.
 */
class UserDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext)
  extends DefaultDAOImpl[User, BSONObjectID] {

  /** Returns a collection of models from database. */
  override protected def collection: Future[BSONCollection] = reactiveMongoApi.database
    .map(db => db.collection("users"))

  /** Returns a query to find a model by id.
   *
   * @param id an object to define a model in database.
   * */
  override protected def idSelector(id: BSONObjectID): BSONDocument = BSONDocument("_id" -> id)

  /** Returns user using login information.
   *
   * @param loginInfo an instance of [[com.mohiva.play.silhouette.api.LoginInfo]]. */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = BSONDocument("email" -> loginInfo.providerKey, "credentialProviderId" -> loginInfo.providerID)
    super.find(query)
  }

  /** Returns user using hashId from secondFactor.
   *
   * @param hashId a hashId from secondFactor. */
  def retrieveByHashId(hashId: String): Future[Option[User]] = {
    val query = BSONDocument("secondFactorLinkingData.hashId" -> hashId)
    super.find(query)
  }

  /** Returns user using second factor id.
   *
   * @param twoFactorId second factor id . */
  def retrieveByTwoFactorId(twoFactorId: String): Future[Option[User]] = {
    val query = BSONDocument("secondFactorLinkingData.twoFactorId" -> twoFactorId)
    super.find(query)
  }
  /** Returns user using an email.
   *
   * @param email second factor id . */
  def retrieveByEmail(email: String): Future[Option[User]] = {
    val query = BSONDocument("email" -> email)
    super.find(query)
  }
}