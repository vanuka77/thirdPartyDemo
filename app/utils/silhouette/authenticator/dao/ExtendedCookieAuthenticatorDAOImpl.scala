package utils.silhouette.authenticator.dao

import models.daos.DefaultDAOImpl
import play.api.Logging
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONArray, BSONDocument, BSONObjectID}
import reactivemongo.play.json.compat.bson2json._
import reactivemongo.play.json.compat.json2bson.toDocumentReader
import utils.silhouette.authenticator.ExtendedCookieAuthenticator

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/** DAO for custom authenticator.
 *
 * @see [[ExtendedCookieAuthenticator]] */
class ExtendedCookieAuthenticatorDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)
                                                  (implicit ex: ExecutionContext)
  extends DefaultDAOImpl[ExtendedCookieAuthenticator, String] with Logging {



  /** Returns a collection of models from database. */
  override def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("authenticators"))

  /** Returns a query to find a model by id.
   *
   * @param id an object to define a model in database.
   * */
  override protected def idSelector(id: String): BSONDocument = BSONDocument("id" -> id)

  /** Deletes a sequence of authenticators using a sequence of ids.
   *
   * @param ids ids of authenticators for deleting. */
  def delete(ids: Seq[BSONObjectID]) = {
    val selector = BSONDocument("_id" -> BSONDocument("$in" -> BSONArray(ids)))

    val futureWriteResult = collection.flatMap(col => col.delete.one(selector))

    futureWriteResult.onComplete {
      case Failure(e) => logger.error("Unable to delete!" + e.getStackTrace.toString)
      case Success(writeResult) => logger.error("Ids for deleting :" + ids + ".Write result : " + writeResult + ".")
    }
  }

}