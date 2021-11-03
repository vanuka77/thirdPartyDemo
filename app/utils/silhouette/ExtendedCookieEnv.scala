package utils.silhouette

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import utils.silhouette.authenticator.ExtendedCookieAuthenticator

/** Defines silhouette environment. */
trait ExtendedCookieEnv extends Env {
  type I = User
  type A = ExtendedCookieAuthenticator
}
