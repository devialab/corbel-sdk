package io.corbel.sdk.auth

import io.corbel.sdk.auth.AuthenticationProvider._
import io.corbel.sdk.error.ApiError

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait UsesAuthentication {
  protected def auth[T](f: String => Future[Either[ApiError,T]])(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError,T]] = authenticationProvider().flatMap(f)
}
