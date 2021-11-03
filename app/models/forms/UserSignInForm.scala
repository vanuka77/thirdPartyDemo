package models.forms

/** A login form. */
object UserSignInForm {

  import play.api.data.Form
  import play.api.data.Forms._

  /** Form data.*/
  case class Data(email: String,
                  password: String)

  val signInForm: Form[Data] = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 4, maxLength = 128),
    )(Data.apply)(Data.unapply)
  )
}
