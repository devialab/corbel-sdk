package io.corbel.sdk.iam

import java.time.Instant

import io.corbel.sdk.config.CorbelConfig
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.concurrent.duration.Duration

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
  val exp = "exp"
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

  //Wrapper classes
  case class RefreshToken(token: String)
  case class TokenExpiration(timestamp: Long)
  object TokenExpiration {
    def apply(duration: Duration): TokenExpiration = TokenExpiration(Instant.now().plusMillis(duration.toMillis).toEpochMilli / 1000)
  }

  def apply() = new Claims()
  def default(implicit corbelConfig: CorbelConfig = null) = Option(corbelConfig) match {
    case Some(config) => audience + TokenExpiration (config.defaultTokenExpiration)
    case None => audience
  }


  def apply(clientCredentials: ClientCredentials) = new Claims(iss -> clientCredentials.clientId)

  private def audience = new Claims(aud -> "http://iam.corbel.io")
}

class Claims(private val json: JObject = JObject()) {
  import Claims._

  def +(clientCredentials: ClientCredentials): Claims = this + (iss -> clientCredentials.clientId)
  def +(refreshToken: RefreshToken): Claims = this + (refresh_token -> refreshToken.token)
  def +(authenticationOptions: AuthenticationOptions): Claims = {
    var claims = JObject()
    for(clientVersion <- authenticationOptions.clientVersion) {
      claims ~= version -> clientVersion
    }
    for(deviceId <- authenticationOptions.deviceId) {
      claims ~= device_id -> deviceId
    }
    this + claims
  }
  def +(userCredentials: UserCredentials): Claims = {
    var claims = JObject()
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
    this + claims
  }
  def +(tokenExpiration: TokenExpiration): Claims = this + (exp -> tokenExpiration.timestamp)
  def +(other: Claims): Claims = this + other.json

  def toJson: String = compact(render(json))

  private def +(other: JObject): Claims = new Claims(json ~ other)


}
