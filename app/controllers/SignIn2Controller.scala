package controllers

import com.google.inject.Inject
import play.api.libs.ws.{BodyWritable, WSClient, WSRequest, WSResponse}
import play.api.mvc.{AnyContent, Request}
import services.SignatureInfosGenerator

import java.io.File
import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class SignIn2Controller @Inject()(ws: WSClient,
                                  scc: SilhouetteControllerComponents
                                 )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {
  def process() = silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
    val generator: SignatureInfosGenerator = new SignatureInfosGenerator()

    val signatureInfos = generator.create()
    val clientId = "612e2dd22200002400f733f4"
    val timestampMessage = signatureInfos._2
    println(timestampMessage)
    val request: WSRequest = ws.url("http://3.133.100.218:9000/authenticator/link/user")
    val authHeader = "ClientId=" + clientId + ";Signature=" + signatureInfos._1
    println(authHeader)

    val complexRequest: WSRequest =
      request
        .addHttpHeaders("Content-Type" -> "application/json", "Auth-Date" -> timestampMessage, "Authorization" -> authHeader)
        .withRequestTimeout(10000.millis)

    val futureResponse: Future[WSResponse] = complexRequest.post("")
    println("sign 2 headers :" + complexRequest.headers)
    futureResponse.map(i => println(i.body))

    Future.successful(Ok("sign in 2 processing ..."))
  }
}
//TODO:joda time
//TODO:finger print see
