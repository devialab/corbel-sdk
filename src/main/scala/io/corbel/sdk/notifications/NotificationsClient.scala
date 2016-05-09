package io.corbel.sdk.notifications

import io.corbel.sdk.auth.AuthenticationProvider._
import io.corbel.sdk.auth.{AutomaticAuthentication, UsesAuthentication}
import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.http.CorbelHttpClient
import io.corbel.sdk.iam._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class NotificationsClient(implicit val config: CorbelConfig) extends CorbelHttpClient with Notifications with UsesAuthentication {

  override def sendNotification(id: String)(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError, Unit]] = auth { token =>
    //todo: this is just an example
    Future.successful(Right())
  }
}

object NotificationsClient {
  def apply()(implicit config: CorbelConfig) = new NotificationsClient()

  def withAutomaticAuthentication(providedClientCredentials: ClientCredentials, providedUserCredentials: Option[UserCredentials] = None , providedAuthenticationOptions: AuthenticationOptions = AuthenticationOptions.default, providedExecutionContext: ExecutionContext = ExecutionContext.global)
                                 (implicit config: CorbelConfig) =
    new NotificationsClient() with AutomaticAuthentication {
      override val clientCredentials: ClientCredentials = providedClientCredentials
      override val userCredentials: Option[UserCredentials] = providedUserCredentials
      override val authenticationOptions: AuthenticationOptions = providedAuthenticationOptions
      override val executionContext: ExecutionContext = providedExecutionContext
      override val authClient: AuthenticationClient = IamClient()
    }
}