package models.daos

import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.Future

trait DefaultDAO[T] {

  protected def collection: Future[BSONCollection]

  def insert(item: T): Future[T]
  def bulkInsert(items: Seq[T], order: Boolean = false): Future[Seq[T]]
  def find(id: String): Future[Option[T]]
  def find(query: BSONDocument): Future[Option[T]]
  def find(ids: List[String]): Future[Seq[T]]
  def update(id: String, newItem: T): Future[Option[T]]
  def remove(id: String): Future[Option[T]]

}

