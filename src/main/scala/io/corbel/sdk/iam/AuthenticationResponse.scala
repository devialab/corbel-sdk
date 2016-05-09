package io.corbel.sdk.iam

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class AuthenticationResponse(
                                   accessToken: String,
                                   expiresAt: Long,
                                   refreshToken: Option[String],
                                   scopes: Seq[String] = Seq.empty
                                 )
