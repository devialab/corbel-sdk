package io.corbel.sdk.auth


import grizzled.slf4j.Logging
import io.corbel.sdk.auth.AuthenticationProvider.AuthenticationProvider
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.iam._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
private [sdk] trait AutomaticAuthentication extends Iam with Logging {
  val clientCredentials: ClientCredentials
  val userCredentials: Option[UserCredentials]
  val authenticationOptions: AuthenticationOptions
  implicit val executionContext: ExecutionContext

  private implicit lazy val authProvider = new AutomaticAuthenticationProvider(clientCredentials, userCredentials, authenticationOptions, this)(executionContext)

  abstract override def addGroupsToUser(userId: String, groups: Iterable[String])(implicit authenticationProvider: AuthenticationProvider = authProvider, ec: ExecutionContext): Future[Either[ApiError, Unit]] =
    withRefreshTokenProvider { p: AuthenticationProvider =>
      super.addGroupsToUser(userId, groups)(p, ec)
    }

  abstract override def createGroup(group: Group)(implicit authenticationProvider: AuthenticationProvider = authProvider, ec: ExecutionContext): Future[Either[ApiError, String]] =
    withRefreshTokenProvider { p: AuthenticationProvider =>
      super.createGroup(group)(p, ec)
    }

  abstract override def getUser(implicit authenticationProvider: AuthenticationProvider = authProvider, ec: ExecutionContext): Future[Either[ApiError, User]] =
    withRefreshTokenProvider { p: AuthenticationProvider =>
      super.getUser(p, ec)
    }

  abstract override def getUserbyId(id: String)(implicit authenticationProvider: AuthenticationProvider = authProvider, ec: ExecutionContext): Future[Either[ApiError, User]] =
    withRefreshTokenProvider { p: AuthenticationProvider =>
      super.getUserbyId(id)(p, ec)
    }


  private def withRefreshTokenProvider[T](block: AuthenticationProvider => Future[Either[ApiError,T]])(implicit authenticationProvider: AuthenticationProvider): Future[Either[ApiError,T]] =
    block(authenticationProvider).flatMap({
      case Left(apiError) if apiError.status == 401  => Future.failed(AuthenticationException(apiError))
      case x => Future.successful(x)
    }).recoverWith {
      case e: AuthenticationException =>
        debug("Using token refresh provider")
        val refreshTokenProvider: AuthenticationProvider = authProvider.authenticationRefreshProvider(e.apiError)
        block(refreshTokenProvider)
    }
}

case class AuthenticationException(apiError: ApiError) extends Exception(apiError.toString)

private class AutomaticAuthenticationProvider(clientCredentials: ClientCredentials, userCredentials: Option[UserCredentials], authenticationOptions: AuthenticationOptions, iam: Iam)(implicit ec: ExecutionContext)
  extends AuthenticationProvider with Logging {

  @volatile var refreshToken: Option[String] = None
  @volatile var accessToken: Option[String] = None

  override def apply(): Future[String] = accessToken match {
    case Some(token) => Future.successful(token)
    case None => handleAuthenticationResponse(iam.authenticate(clientCredentials, userCredentials, authenticationOptions))
  }

  def authenticationRefreshProvider(apiError: ApiError): AuthenticationProvider = refreshToken match {
    case None => () => Future.failed(AuthenticationException(apiError))
    case Some(token) => () => handleAuthenticationResponse(iam.authenticationRefresh(clientCredentials, token, authenticationOptions))
  }

  private def handleAuthenticationResponse(f: Future[Either[ApiError, AuthenticationResponse]]): Future[String] = f.flatMap {
    case Right(authResponse) =>
      refreshToken = authResponse.refreshToken
      accessToken = Some(authResponse.accessToken)
      Future.successful(authResponse.accessToken)
    case Left(apiError) =>
      throw AuthenticationException(apiError)
  }
}
