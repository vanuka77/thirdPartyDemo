package services

import org.joda.time.{DateTime, DateTimeZone}
import services.SignatureInfosGenerator.{encoding, kf, privateKeyString, signatureECDSA}

import java.security.{KeyFactory, Signature}
import java.security.spec.PKCS8EncodedKeySpec
import java.util.{Base64, Date}

class SignatureInfosGenerator {
  def create() = {
    val timestampMessage = new DateTime(DateTimeZone.UTC).getMillis.toString

    val privKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder.decode(privateKeyString))

    val privateKey = kf.generatePrivate(privKeySpec)

    val signatureInstance = Signature.getInstance(signatureECDSA)

    signatureInstance.initSign(privateKey)

    signatureInstance.update(timestampMessage.getBytes(encoding))

    val signature = signatureInstance.sign()

    val signatureString = Base64.getEncoder.encodeToString(signature)

    (signatureString, timestampMessage)
  }
}

object SignatureInfosGenerator {
  val privateKeyString = "MD4CAQAwEAYHKoZIzj0CAQYFK4EEAAoEJzAlAgEBBCAua+T0a8jQQy9EdYYEkv5zCrDzILd8uEB9bif8Dn99Lg=="
  val publicKeyString = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAESyFmCV5/UKjN/1HOprcbS/apa/76ejAKPAdlf0lGrzLzaZ8OBnfu9y/gtuBZKJ/aqEcBGLnwETl9kZ6q/OtOig=="
  val signatureECDSA = "SHA256withECDSA"
  val kf = KeyFactory.getInstance("EC")
  val encoding = "UTF-8"
}
