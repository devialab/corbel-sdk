package io.corbel.sdk.iam

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class AuthenticationOptions(clientVersion: Option[String] = None, deviceId: Option[String] = None)

object AuthenticationOptions {
  val default = AuthenticationOptions()
}