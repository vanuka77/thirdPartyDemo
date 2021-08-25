package models.daos

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class AuthenticatorRepImpl @Inject()(val cookieAuthenticatorDAO: CookieAuthenticatorDAO)(implicit ex: ExecutionContext) extends AuthenticatorRepository[CookieAuthenticator] {

  override def find(id: String): Future[Option[CookieAuthenticator]] = cookieAuthenticatorDAO.find(id)

  override def add(authenticator: CookieAuthenticator): Future[CookieAuthenticator] = cookieAuthenticatorDAO.insert(authenticator)

  override def update(authenticator: CookieAuthenticator): Future[CookieAuthenticator] = cookieAuthenticatorDAO.update(authenticator.id, authenticator).map(_ => authenticator)

  override def remove(id: String): Future[Unit] = cookieAuthenticatorDAO.remove(id).map(_ => ())
}


