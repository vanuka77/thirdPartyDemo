package utils.silhouette

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import play.twirl.api.Html

import javax.inject.Inject
import scala.concurrent.Future

/**
 * Custom secured error handler.
 *
 * @param messagesApi The Play messages API.
 */
class CustomSecuredErrorHandler @Inject()(val messagesApi: MessagesApi) extends SecuredErrorHandler with I18nSupport {

  /**
   * Called when a userSignUp is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(implicit request: RequestHeader) = {
    Future.successful(Ok(views.html.onNotAuthenticated()))
  }

  /**
   * Called when a userSignUp is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(implicit request: RequestHeader) = {
    Future.successful(Ok(views.html.onNotAuthorized()))
  }
}