package models.forms

object UserRegistrationForm {

  import play.api.data.Form
  import play.api.data.Forms._

  case class Data(email: String,
                  name: String,
                  lastName: String,
                  password: String)

  val form: Form[Data] = Form(
    mapping(
      "email" -> email,
      "name" -> nonEmptyText,
      "lastName" ->nonEmptyText,
      "password" -> nonEmptyText(minLength = 4, maxLength = 128),
    )(Data.apply)(Data.unapply)
  )

}