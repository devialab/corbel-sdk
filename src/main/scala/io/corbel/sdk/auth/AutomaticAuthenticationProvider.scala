package io.corbel.sdk.auth


import grizzled.slf4j.Logging
import io.corbel.sdk.api.RequestParams
import io.corbel.sdk.auth.AuthenticationProvider.AuthenticationProvider
import io.corbel.sdk.config.HasConfig
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.iam._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
private [sdk] trait AutomaticAuthentication extends Logging { self: UsesAuthentication with HasConfig =>

  val clientCredentials: ClientCredentials
  val userCredentials: Option[UserCredentials]
  val authenticationOptions: AuthenticationOptions
  val executionContext: ExecutionContext
  val authClient: AuthenticationClient

  private lazy val authProvider = new AutomaticAuthenticationProvider(clientCredentials, userCredentials, authenticationOptions, authClient)(executionContext)


  abstract override protected def auth[T](f: (String) => Future[Either[ApiError,T]])(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError,T]] = {
    Option(authenticationProvider) match {
      case Some(ap) => self.auth(f)
      case None => {
        implicit val authenticationProvider = authProvider
        withRefreshTokenProvider { ap =>
          ap().flatMap(f)
        }
      }
    }
  }

  private def withRefreshTokenProvider[T](block: AuthenticationProvider => Future[Either[ApiError,T]])(implicit ec: ExecutionContext): Future[Either[ApiError,T]] =
    block(authProvider).flatMap({
      case Left(apiError) if apiError.status == 401  => Future.failed(AuthenticationException(apiError))
      case x => Future.successful(x)
    }).recoverWith {
      case e: AuthenticationException =>
        //Refresh token is only applicable to user tokens
        if(userCredentials.isDefined) {
          debug("Using token refresh provider")
          val refreshTokenProvider: AuthenticationProvider = authProvider.authenticationRefreshProvider(e.apiError)
          block(refreshTokenProvider)
        }
        else {
          //reset token to force
          authProvider.accessToken = None
          block(authProvider)
        }
    }
}

case class AuthenticationException(apiError: ApiError) extends Exception(apiError.toString)

private class AutomaticAuthenticationProvider(clientCredentials: ClientCredentials, userCredentials: Option[UserCredentials], authenticationOptions: AuthenticationOptions, authClient: AuthenticationClient)(implicit ec: ExecutionContext)
  extends AuthenticationProvider with Logging {

  @volatile var refreshToken: Option[String] = None
  @volatile var accessToken: Option[String] = None

  override def apply(): Future[String] = accessToken match {
    case Some(token) => Future.successful(token)
    case None => handleAuthenticationResponse(authClient.authenticate(clientCredentials, userCredentials, authenticationOptions))
  }

  def authenticationRefreshProvider(apiError: ApiError): AuthenticationProvider = refreshToken match {
    case None => () => Future.failed(AuthenticationException(apiError))
    case Some(token) => () =>
      debug(s"Using refresh token $refreshToken")
      handleAuthenticationResponse(authClient.authenticationRefresh(clientCredentials, token, authenticationOptions))
  }

  private def handleAuthenticationResponse(f: Future[Either[ApiError, AuthenticationResponse]]): Future[String] = f.flatMap {
    case Right(authResponse) =>
      debug(s"Authenticated in with the following scopes: ${authResponse.scopes}")
      refreshToken = authResponse.refreshToken
      accessToken = Some(authResponse.accessToken)
      Future.successful(authResponse.accessToken)
    case Left(apiError) =>
      throw AuthenticationException(apiError)
  }
}
