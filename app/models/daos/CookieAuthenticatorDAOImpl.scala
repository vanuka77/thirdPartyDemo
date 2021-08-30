package models.daos

import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.play.json.compat.bson2json._
import reactivemongo.play.json.compat.json2bson.toDocumentReader

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CookieAuthenticatorDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends DefaultDAOImpl[CookieAuthenticator] {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("authenticators"))

  override protected def idSelector(id: String): BSONDocument = BSONDocument("id" -> id)
}
