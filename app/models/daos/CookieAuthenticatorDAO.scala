package models.daos

import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat, Writes}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.{BSONDocumentReader, Macros}
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.play.json.compat.bson2json._
import reactivemongo.play.json.compat.json2bson.toDocumentReader

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CookieAuthenticatorDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends DefaultDAOImpl[CookieAuthenticator] {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("authenticators"))
}
