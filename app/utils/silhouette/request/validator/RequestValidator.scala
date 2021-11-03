package utils.silhouette.request.validator

import com.google.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.Request
import utils.silhouette.request.signature.SignatureVerifier

class RequestValidator @Inject()(signatureVerifier: SignatureVerifier) {
  def validate(request: Request[JsValue]): Boolean = {
    println(request.headers)
    val signatureHeader: String = request.headers.get("Authorization").get
    println(signatureHeader)
    val signature = signatureHeader.substring(10)
    val timeStamp = request.headers.get("Auth-Date").getOrElse("")
    println(timeStamp)
    signatureVerifier.verify(signature, timeStamp)
  }
}
