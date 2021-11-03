package utils.silhouette.authenticator.task


import javax.inject.Inject
import javax.inject.Named
import akka.actor.ActorRef
import akka.actor.ActorSystem
import reactivemongo.api.bson.BSONDocument
import utils.silhouette.authenticator.dao.ExtendedCookieAuthenticatorDAOImpl

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/** Asynchronous task for deleting expired authenticators. */
class AutoDeletingAuthenticatorsTask @Inject()(actorSystem: ActorSystem,
                                               extendedCookieAuthenticatorDAO: ExtendedCookieAuthenticatorDAOImpl)
                                              (implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 1.day) {
    process()
  }

  /** Deletes expired authenticators. */
  def process(): Unit = {
    val authenticators = extendedCookieAuthenticatorDAO.getAll()
    authenticators.map(list => list.filter(a => a.isExpired).map(_._id)).map(listForDelete => {
      extendedCookieAuthenticatorDAO.delete(listForDelete)
    })
  }
}