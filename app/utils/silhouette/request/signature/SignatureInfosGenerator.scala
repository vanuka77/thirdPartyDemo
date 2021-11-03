package utils.silhouette.request.signature

import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration

import java.security.spec.PKCS8EncodedKeySpec
import java.security.{KeyFactory, Signature}
import java.util.Base64
import javax.inject.Inject

/** A generator of a signature. */
class SignatureInfosGenerator @Inject()(config: Configuration) {
  /** Creates a signature.
   *
   * @return a tuple with signature and time stamp. */
  def create() = {
    val timestampMessage = new DateTime(DateTimeZone.UTC).getMillis.toString

    val privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder.decode(config.get[String]("factor2.privateKey")))

    val kf = KeyFactory.getInstance(config.get[String]("signature.typeKF"))

    val privateKey = kf.generatePrivate(privateKeySpec)

    val signatureInstance = Signature.getInstance(config.get[String]("signature.type"))

    signatureInstance.initSign(privateKey)

    signatureInstance.update(timestampMessage.getBytes(config.get[String]("signature.encoding")))

    val signature = signatureInstance.sign()

    val signatureString = Base64.getEncoder.encodeToString(signature)
    (signatureString, timestampMessage)
  }
}