package models.daos

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.api.Cursor
import reactivemongo.api.bson.{BSONArray, BSONDocument, BSONDocumentReader, BSONObjectID}
import reactivemongo.play.json.compat,
compat.json2bson._,
compat.bson2json._
import scala.concurrent.{ExecutionContext, Future}


abstract class DefaultDAOImpl[T](implicit val jsonFormat: OFormat[T],
                                 implicit val reader: BSONDocumentReader[T],
                                 implicit val exec: ExecutionContext) extends DefaultDAO[T] {

  protected def idSelector(id: String): BSONDocument = BSONDocument("_id" -> BSONObjectID.parse(id).get)

  override def insert(item: T): Future[T] = {
    collection.flatMap(_.insert.one(item)).map { _ => item }
  }

  override def bulkInsert(items: Seq[T], order: Boolean = false): Future[Seq[T]] = {
    collection.flatMap { coll =>
      coll.insert(ordered = order).many(items).map(_ => items)
    }
  }

  override def find(id: String): Future[Option[T]] = {
    collection.flatMap(col => {
      col.find(idSelector(id)).one[T]
    })
  }

  override def find(query: BSONDocument): Future[Option[T]] = {
    collection.flatMap(col => {
      col.find(query).one[T]
    })
  }

  override def find(ids: List[String]): Future[Seq[T]] = {
    val query = BSONDocument("_id" -> BSONDocument("$in" -> BSONArray(ids.map(BSONObjectID.parse(_).get))))
    collection.flatMap(col => {
      col.find(query).cursor[T]().collect[Seq](100, Cursor.FailOnError[Seq[T]]())
    })
  }

  override def update(id: String, newItem: T): Future[Option[T]] = {
    collection.flatMap(_.findAndUpdate(idSelector(id), newItem, true) map (res => res.result[T]))
  }

  override def remove(id: String): Future[Option[T]] = {
    collection.flatMap(col => {
      col.findAndRemove(idSelector(id)).map(_.result[T])
    })
  }

  protected def optionQuery[A](field: Option[A])(genQuery: => BSONDocument): BSONDocument = {
    field match {
      case Some(f) => genQuery
      case None => BSONDocument.empty
    }
  }
}
