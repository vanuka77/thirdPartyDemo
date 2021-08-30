package models.forms

object UserLoginForm {
  import play.api.data.Form
  import play.api.data.Forms._

  case class Data(email: String,
                  password: String)

  val form: Form[Data] = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 4, maxLength = 128),
    )(Data.apply)(Data.unapply)
  )
}
