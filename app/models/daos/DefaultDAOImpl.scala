package models.daos

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader}
import reactivemongo.play.json.compat.bson2json._
import reactivemongo.play.json.compat.json2bson._

import scala.concurrent.{ExecutionContext, Future}

/** A DAO template with default realization.
 *
 * @tparam T  type of model to work with.
 * @tparam ID type of id used to define a model in database([[reactivemongo.api.bson.BSONObjectID]] or [[String]]).
 * */
abstract class DefaultDAOImpl[T, ID](implicit val jsonFormat: OFormat[T],
                                     implicit val reader: BSONDocumentReader[T],
                                     implicit val exec: ExecutionContext) extends DefaultDAO[T, ID] {

  /** Inserts a model into the collection.
   *
   * @param item the model to insert
   * @return an inserted model.
   * */
  override def insert(item: T): Future[T] = {
    collection.flatMap(_.insert.one(item)).map { _ => item }
  }

  /** Inserts a sequence of models into the collection.
   *
   * @param items the sequence of models.
   * @param order should be ordered or not.
   * @return an inserted models.
   * */
  override def bulkInsert(items: Seq[T], order: Boolean = false): Future[Seq[T]] = {
    collection.flatMap { coll =>
      coll.insert(ordered = order).many(items).map(_ => items)
    }
  }

  /** Returns a model with the given id.
   *
   * @param id an object to define a model in database.
   * */
  override def find(id: ID): Future[Option[T]] = {
    collection.flatMap(col => {
      col.find(idSelector(id)).one[T]
    })
  }

  /** Returns models fits the query .
   *
   * @param query a query to database. */
  override def find(query: BSONDocument): Future[Option[T]] = {
    collection.flatMap(col => {
      col.find(query).one[T]
    })
  }

  /** Updates a model with the given id .
   *
   * @param id      an object to define a model in database.
   * @param newItem a new model.
   * @return an updated model from database.
   * */
  override def update(id: ID, newItem: T): Future[Option[T]] = {
    collection.flatMap(_.findAndUpdate(idSelector(id), newItem, true) map (res => res.result[T]))
  }

  /** Removes a model with the given id .
   *
   * @param id an object to define a model in database.
   * @return a removed model. */
  override def remove(id: ID): Future[Option[T]] = {
    collection.flatMap(col => {
      col.findAndRemove(idSelector(id)).map(_.result[T])
    })
  }

  /** Returns all models in the collection .
   *
   * @param offset an amount of models to skip from the beginning. */
  override def getAll(offset: Option[Int] = None): Future[Seq[T]] = {
    collection.flatMap(col => {
      col.find(BSONDocument.empty).skip(offset.getOrElse(0)).cursor[T]().collect[Seq]()
    })
  }

}
