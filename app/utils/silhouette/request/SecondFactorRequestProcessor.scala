package utils.silhouette.request

import com.google.inject.Inject
import play.api.{Configuration, Logging}
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSRequest}
import utils.silhouette.request.signature.SignatureInfosGenerator

import scala.concurrent.duration.DurationInt

class SecondFactorRequestProcessor @Inject()(
                                              ws: WSClient,
                                              generator: SignatureInfosGenerator,
                                              config: Configuration
                                            ) {
  def process(url: String, data: JsObject = null, methodType: String = "post", queryParam: (String, String) = ("", "")) = {
    val signatureInfos = generator.create()
    val timestampMessage = signatureInfos._2
    val request: WSRequest = ws.url(url)
    val authHeader = "ClientId=" + config.get[String]("factor2.clientId") + ";Signature=" + signatureInfos._1
    val complexRequest: WSRequest = {
      request
        .addHttpHeaders(
          "Content-Type" -> "application/json",
          "Auth-Date" -> timestampMessage,
          "Authorization" -> authHeader
        )
        .addQueryStringParameters(queryParam)
        .withRequestTimeout(10000.millis)
    }
    methodType match {
      case "post" => complexRequest.post(data)
      case "delete" => complexRequest.delete()
      case "get" => complexRequest.get()
    }
  }
}
