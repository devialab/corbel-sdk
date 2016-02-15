package io.corbel.sdk

import io.corbel.sdk.iam.AuthenticationResponse

/**
  * Wraps a factory function which returns a Corbel access token
  *
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class AuthenticationProvider(f: => String) extends (() => String) {
  override def apply(): String = f
}

object AuthenticationProvider {
  /**
    * Create a AuthenticationProvider from an AuthenticationResponse
    *
    * @param authResponse
    * @return
    */
  def apply(authResponse: AuthenticationResponse): AuthenticationProvider = AuthenticationProvider {
    authResponse.accessToken
  }

  def apply(f: => String): AuthenticationProvider = new AuthenticationProvider(f)
}