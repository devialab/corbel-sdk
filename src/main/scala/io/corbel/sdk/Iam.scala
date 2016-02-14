package io.corbel.sdk

import _root_.scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait Iam {

  def authenticate(clientCredentials: ClientCredentials,
                   userCredentials: Option[UserCredentials] = None,
                   authenticationOptions: AuthenticationOptions = AuthenticationOptions.default)
                    (implicit ec: ExecutionContext):Future[Either[ApiError,AuthenticationResponse]]

  def authenticationRefresh(clientCredentials: ClientCredentials,
                            refreshToken: String,
                            authenticationOptions: AuthenticationOptions = AuthenticationOptions.default)
                            (implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]]

}


