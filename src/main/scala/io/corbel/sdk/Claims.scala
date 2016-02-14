package io.corbel.sdk

import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

/**
  * Claims is a builder for the JWT claims needed to authenticate with Corbel IAM
  *
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
object Claims {

  val aud = "aud"
  val iss = "iss"
  val refresh_token = "refresh_token"
  val version = "version"
  val device_id = "device_id"
  val prn = "prn"
  object oauth {
    val service = "oauth.service"
    val redirect_uri = "oauth.redirect_uri"
    val code = "oauth.code"
    val access_token = "oauth.access_token"
    val token = "oauth.token"
    val verifier = "oauth.verifier"
  }
  object basic_auth {
    val username = "basic_auth.username"
    val password = "basic_auth.password"
  }

  def apply() = new Claims
}

class Claims(c: JObject = Claims.aud -> "http://iam.corbel.io") {
  import Claims._
  def addClientCredentials(clientCredentials: ClientCredentials) =
    new Claims(c ~ (iss -> clientCredentials.clientId))

  def addRefreshToken(refreshToken: String) =
    new Claims (c ~ (refresh_token -> refreshToken))

  def addOptions(authenticationOptions: AuthenticationOptions) = {
    var claims = c
    for(clientVersion <- authenticationOptions.clientVersion) {
      claims ~= version -> clientVersion
    }
    for(deviceId <- authenticationOptions.deviceId) {
      claims ~= device_id -> deviceId
    }
    new Claims(claims)
  }

  def addUserCredentials(userCredentials: UserCredentials) = {
    var claims = c
    userCredentials match {
      case oauth2: Oauth2UserCredentials =>
        claims ~= (oauth.service -> oauth2.service) ~ (oauth.redirect_uri -> oauth2.redirectUri)
        oauth2.proof match {
          case code: Oauth2Code =>
            claims ~= oauth.code -> code.code
          case token: Oauth2AccessToken =>
            claims ~= oauth.access_token -> token.accessToken
        }
      case oauth1: Oauth1UserCredentials =>
        claims ~= (oauth.service -> oauth1.service) ~ (oauth.token -> oauth1.token) ~ (oauth.verifier -> oauth1.verifier)
      case basic: BasicUserCredentials =>
        claims ~= (basic_auth.username -> basic.username) ~ (basic_auth.password -> basic.password)
      case delegated: DelegatedUserCredentials =>
        claims ~= prn -> delegated.principal
    }
    new Claims(claims)
  }

  def toJson: String = compact(render(c))
}
