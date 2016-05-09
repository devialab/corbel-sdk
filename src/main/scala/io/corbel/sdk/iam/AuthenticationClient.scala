package io.corbel.sdk.iam

import io.corbel.sdk.error.ApiError

import scala.concurrent.{Future, ExecutionContext}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait AuthenticationClient {

  /* ----------------- Authentication ------------ */
  def authenticate(clientCredentials: ClientCredentials,
                   userCredentials: Option[UserCredentials] = None,
                   authenticationOptions: AuthenticationOptions = AuthenticationOptions.default)
                  (implicit ec: ExecutionContext):Future[Either[ApiError,AuthenticationResponse]]

  def authenticationRefresh(clientCredentials: ClientCredentials,
                            refreshToken: String,
                            authenticationOptions: AuthenticationOptions = AuthenticationOptions.default)
                           (implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]]
}
