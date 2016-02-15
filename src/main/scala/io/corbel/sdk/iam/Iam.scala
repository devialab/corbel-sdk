package io.corbel.sdk.iam

import io.corbel.sdk.AuthenticationProvider
import io.corbel.sdk.error.ApiError

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait Iam {

  /* ----------------- Authentication ------------ */
  def authenticate(clientCredentials: ClientCredentials,
                   userCredentials: Option[UserCredentials] = None,
                   authenticationOptions: AuthenticationOptions = AuthenticationOptions.default)
                    (implicit ec: ExecutionContext):Future[Either[ApiError,AuthenticationResponse]]

  def authenticationRefresh(clientCredentials: ClientCredentials,
                            refreshToken: String,
                            authenticationOptions: AuthenticationOptions = AuthenticationOptions.default)
                            (implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]]

  /* ----------------- Users ---------------------- */

  def getUser(id: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError,User]]

  def getUser(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError,User]]

  /* ----------------- Groups ---------------------- */
}


