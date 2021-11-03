package utils.silhouette.request.signature

import play.api.Configuration

import java.security.spec.X509EncodedKeySpec
import java.security.{KeyFactory, Signature}
import java.util.Base64
import javax.inject.Inject

class SignatureVerifier @Inject() (config: Configuration) {
  def verify(signature: String, timestamp: String): Boolean = {
    val mindAuthPublicKeyString = config.get[String]("factor2.publicKey")
    val signatureECDSA = config.get[String]("signature.type")
    val pubKeySpec = new X509EncodedKeySpec(
      Base64.getDecoder.decode(mindAuthPublicKeyString)
    )
    val kf = KeyFactory.getInstance(config.get[String]("signature.typeKF"))
    val mindAuthPubKey = kf.generatePublic(pubKeySpec)
    val signatureInstance = Signature.getInstance(signatureECDSA)
    signatureInstance.initVerify(mindAuthPubKey)
    signatureInstance.update(timestamp.getBytes("UTF-8"))
    signatureInstance.verify(Base64.getDecoder.decode(signature))
    true
  }
}
