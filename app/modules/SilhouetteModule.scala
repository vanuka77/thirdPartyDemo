package modules

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.actions.{SecuredAction, SecuredErrorHandler, UnsecuredAction, UnsecuredErrorHandler, UserAwareAction}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.{AuthInfoRepository, AuthenticatorRepository}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import controllers.{DefaultSilhouetteControllerComponents, SilhouetteControllerComponents}
import models.daos.UserDAOImpl
import models.daos.silhouette.PasswordInfoImpl
import utils.auth.{CustomSecuredErrorHandler, CustomUnsecuredErrorHandler}
//import models.daos.UserDAO
//import models.daos.impl.PasswordInfoImpl
import models.services.UserService
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.mvc.CookieHeaderEncoding
import utils.auth.CookieEnv

//import util.auth.CookieEnv
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.DurationConverters._

/** Using for silhouette bindings. */
class SilhouetteModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator())
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the authenticator.
   */
  @Provides
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    new JcaCrypter(JcaCrypterSettings(configuration.underlying.getString("play.http.secret.key")))
  }

  /**
   * Provides the signer for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The signer for the authenticator.
   */
  @Provides
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    new JcaSigner(JcaSignerSettings(configuration.underlying.getString("play.http.secret.key")))
  }

  @Provides
  def provideCookieAuthenticatorSettings(configuration: Configuration): CookieAuthenticatorSettings = {
    CookieAuthenticatorSettings(
      configuration.underlying.getString("authenticator.cookieName"),
      configuration.underlying.getString("authenticator.cookiePath"),
      None,
      configuration.underlying.getBoolean("authenticator.secureCookie"),
      configuration.underlying.getBoolean("authenticator.httpOnlyCookie"),
      None,
      configuration.underlying.getBoolean("authenticator.useFingerprinting"),
      None,
      Some(configuration.underlying.getDuration("authenticator.authenticatorIdleTimeout").toScala),
      configuration.underlying.getDuration("authenticator.authenticatorExpiry").toScala)
  }

  @Provides
  def provideAuthenticationService(crypter: Crypter,
                                   signer: Signer,
                                   cookieHeaderEncoding: CookieHeaderEncoding,
                                   fingerprintGenerator: FingerprintGenerator,
                                   authenticatorRepository: AuthenticatorRepository[CookieAuthenticator],
                                   idGenerator: IDGenerator,
                                   cookieAuthenticatorSettings: CookieAuthenticatorSettings,
                                   clock: Clock): AuthenticatorService[CookieAuthenticator] = {
    val encoder = new CrypterAuthenticatorEncoder(crypter)
    new CookieAuthenticatorService(cookieAuthenticatorSettings, Some(authenticatorRepository), signer,
      cookieHeaderEncoding, encoder, fingerprintGenerator, idGenerator, clock)
  }

  /**
   * Provides auth info delegable auth info repository.
   *
   * @param userDao Operations with user table in database
   * @return DelegableAuthInfoDAO implementation
   */
  @Provides
  def providePasswordDAO(userDao: UserDAOImpl): DelegableAuthInfoDAO[PasswordInfo] = new PasswordInfoImpl(userDao)


  /**
   * Provides the auth info repository.
   *
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @return The auth info repository instance.
   */
  @Provides
  def provideAuthInfoRepository(passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAO)
  }

  /**
   * Provides the password hasher registry.
   *
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository     The auth info repository implementation.
   * @param passwordHasherRegistry The password hasher registry.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(authInfoRepository: AuthInfoRepository,
                                 passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }


  /**
   * Provides cookie authentication environment.
   *
   * @return cookie auth environment.
   */
  @Provides
  def provideEnvironment(userService: UserService,
                         authenticatorService: AuthenticatorService[CookieAuthenticator],
                         eventBus: EventBus): Environment[CookieEnv] = {
    Environment[CookieEnv](userService, authenticatorService, Seq(), eventBus)
  }

  /**
   * Provides silhouette action builder.
   *
   * @return silhouette action builder with cookie auth environment.
   */
  @Provides
  def provideSilhouetteProvider(environment: Environment[CookieEnv], securedAction: SecuredAction, unsecuredAction: UnsecuredAction,
                                userAwareAction: UserAwareAction): Silhouette[CookieEnv] = {
    new SilhouetteProvider[CookieEnv](environment, securedAction, unsecuredAction, userAwareAction)
  }

  /**
   * Provides silhouette components
   *
   * @param components silhouette components implementation
   * @return silhouette components implementation
   */
  @Provides
  def providesSilhouetteComponents(components: DefaultSilhouetteControllerComponents): SilhouetteControllerComponents = {
    components
  }

}
