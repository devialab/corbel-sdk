package io.corbel.sdk.iam

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
sealed trait UserCredentials

case class Oauth2UserCredentials(service: String, proof: Oauth2Proof, redirectUri: String) extends UserCredentials
case class Oauth1UserCredentials(service: String, token: String, verifier: String) extends UserCredentials
case class BasicUserCredentials(username: String, password: String) extends UserCredentials
case class DelegatedUserCredentials(principal: String) extends UserCredentials

sealed trait Oauth2Proof
case class Oauth2Code(code: String) extends Oauth2Proof
case class Oauth2AccessToken(accessToken: String) extends Oauth2Proof