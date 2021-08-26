package models.daos

import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat, Writes}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, Macros}
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.play.json.compat.bson2json._
import reactivemongo.play.json.compat.json2bson.{toDocumentReader, toDocumentWriter}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CookieAuthenticatorDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends DefaultDAOImpl[CookieAuthenticator] {
  def collection: Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("authenticators"))

  override def remove(id: String): Future[Option[CookieAuthenticator]] = {
    collection.flatMap(col => {
      col.findAndRemove(BSONDocument("id" -> id)).map(_.result[CookieAuthenticator])
    })
  }

  override def find(id: String): Future[Option[CookieAuthenticator]] = {
    val query = BSONDocument("_id" -> id)
    super.find(query)
  }

  override def insert(authenticator: CookieAuthenticator): Future[CookieAuthenticator] = super.insert(authenticator)

  def update(authenticator: CookieAuthenticator): Future[CookieAuthenticator] = {
    val query = BSONDocument("id" -> authenticator.id)
    val x = super.find(query)
    collection.flatMap(_.findAndUpdate(query, authenticator, true) map (_ => authenticator))

  }

  override def update(id: String, authenticator: CookieAuthenticator): Future[Option[CookieAuthenticator]] = {
    val query = BSONDocument("_id" -> authenticator.id)
    collection.flatMap(_.findAndUpdate(query, authenticator, true) map (res => res.result[CookieAuthenticator]))
  }
}
