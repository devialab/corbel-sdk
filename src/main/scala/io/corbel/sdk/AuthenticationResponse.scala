package io.corbel.sdk

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class AuthenticationResponse(accessToken: String, expiresAt: Long, refreshToken: Option[String])
