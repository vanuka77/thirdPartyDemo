package models.daos

import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.Future

/** A DAO template.
 *
 * @tparam T  type of model to work with.
 * @tparam ID type of id used to define a model in database([[reactivemongo.api.bson.BSONObjectID]] or [[String]]).
 * */
trait DefaultDAO[T, ID] {

  /** Returns a collection of models from database. */
  protected def collection: Future[BSONCollection]

  /** Returns a query to find a model by id.
   *
   * @param id an object to define a model in database.
   * */
  protected def idSelector(id: ID): BSONDocument

  /** Inserts a model into the collection.
   *
   * @param item the model to insert
   * @return an inserted model.
   * */
  def insert(item: T): Future[T]

  /** Inserts a sequence of models into the collection.
   *
   * @param items the sequence of models.
   * @param order should be ordered or not.
   * @return an inserted models.
   * */
  def bulkInsert(items: Seq[T], order: Boolean = false): Future[Seq[T]]

  /** Returns a model with the given id.
   *
   * @param id an object to define a model in database.
   * */
  def find(id: ID): Future[Option[T]]

  /** Returns models fits the query .
   *
   * @param query a query to database. */
  def find(query: BSONDocument): Future[Option[T]]

  /** Updates a model with the given id .
   *
   * @param id      an object to define a model in database.
   * @param newItem an updated model.
   * @return an updated model.
   * */
  def update(id: ID, newItem: T): Future[Option[T]]

  /** Removes a model with the given id .
   *
   * @param id an object to define a model in database.
   * @return a removed model. */
  def remove(id: ID): Future[Option[T]]

  /** Returns all models in the collection .
   *
   * @param offset an amount of models to skip from the beginning. */
  def getAll(offset: Option[Int] = None): Future[Seq[T]]
}

