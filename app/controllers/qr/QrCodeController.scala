package controllers.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import java.io.{ByteArrayOutputStream, File}
import java.nio.file.Files
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

/** Controller handles requests for creating qr code pictures. */
@Singleton
class QrCodeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /** Creates a qr code picture.
   *
   * @param text   any text for converting into qr code.
   * @param width  width of picture.
   * @param height height of picture.
   * @return a picture as http response.
   * */
  def create(text: String, width: Int, height: Int) = Action {
    implicit request: Request[AnyContent] =>
      val qrCodeWriter = new QRCodeWriter
      val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
      val pngOutputStream = new ByteArrayOutputStream
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream)
      val pngData = pngOutputStream.toByteArray
      val temp = new File("qrcode.png")
      Files.write(temp.toPath, pngData)
      Ok.sendFile(temp)
  }

}