package io.corbel.sdk.auth

import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.iam._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Wraps a factory function which returns a Corbel access token
  *
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
object AuthenticationProvider {
  type AuthenticationProvider = (() => Future[String])
  /**
    * Create a AuthenticationProvider from an AuthenticationResponse
    *
    * @param authResponse
    * @return
    */
  def apply(authResponse: AuthenticationResponse): AuthenticationProvider = AuthenticationProvider {
    authResponse.accessToken
  }

  def apply(f: => String): AuthenticationProvider = () => Future.successful(f)
}