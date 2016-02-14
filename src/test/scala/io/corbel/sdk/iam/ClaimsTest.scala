package io.corbel.sdk.iam

import org.json4s.JsonAST.{JString, JValue}
import org.json4s.native.JsonMethods._
import org.scalatest.{FlatSpec, Matchers}
/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class ClaimsTest extends FlatSpec with Matchers {

  behavior of "Claims"

  it should "add client iss" in {
    val clientId = "123"
    val clientCreds = ClientCredentials(clientId, "secret")
    val claims = Claims().addClientCredentials(clientCreds)
    val json = parse(claims.toJson)
    shouldHaveAudience(json)
    shouldHaveIss(json, clientId)
  }

  it should "add refresh_token" in {
    val refreshToken = "123"
    val claims = Claims().addRefreshToken(refreshToken)
    val json = parse(claims.toJson)
    shouldHaveAudience(json)
    shouldHaveRefreshToken(json, refreshToken)
  }

  it should "add options" in {
    val clientVersion = "1.0"
    val deviceId = "123"
    val options = AuthenticationOptions(Some(clientVersion), Some(deviceId))
    val claims = Claims().addOptions(options)
    val json = parse(claims.toJson)
    shouldHaveAudience(json)
    shouldHaveVersion(json, clientVersion)
    shouldHaveDeviceId(json, deviceId)
  }

  it should "add basic user credentials" in {
    val userCreds = BasicUserCredentials("test-user", "secret")
    val claims = Claims().addUserCredentials(userCreds)
    val json = parse(claims.toJson)
    shouldHaveAudience(json)
    shouldHavePassword(json, userCreds.password)
    shouldHaveUsername(json, userCreds.username)
  }

  it should "add oauth1 user credentials" in {
    val userCreds = Oauth1UserCredentials("twitter", "1234", "567")
    val claims = Claims().addUserCredentials(userCreds)
    val json = parse(claims.toJson)
    shouldHaveAudience(json)
    shouldHaveService(json, userCreds.service)
    shouldHaveToken(json, userCreds.token)
    shouldHaveVerifier(json, userCreds.verifier)
  }

  it should "add oauth2 user credentials" in {
    val userCreds = Oauth2UserCredentials("facebook", Oauth2AccessToken("access"), "redirect")
    val claims = Claims().addUserCredentials(userCreds)
    val json = parse(claims.toJson)
    shouldHaveAudience(json)
    shouldHaveService(json, userCreds.service)
    shouldHaveAccessToken(json, "access")
    shouldHaveRedirectUri(json, userCreds.redirectUri)

    val userCreds2 = Oauth2UserCredentials("facebook", Oauth2Code("_code"), "redirect")
    val json2 = parse(Claims().addUserCredentials(userCreds2).toJson)
    shouldHaveCode(json2, "_code")
  }

  /* -------------- assertions --- */
  def shouldHaveAudience(json: JValue) =
    hasValue(json,"aud","http://iam.corbel.io")

  def shouldHaveIss(json: JValue, value: String) =
    hasValue(json,"iss",value)

  def shouldHaveRefreshToken(json: JValue, value: String) =
    hasValue(json,"refresh_token",value)

  def shouldHaveVersion(json: JValue, value: String) =
    hasValue(json,"version",value)

  def shouldHaveDeviceId(json: JValue, value: String) =
    hasValue(json,"device_id",value)

  def shouldHaveUsername(json: JValue, value: String) =
    hasValue(json,"basic_auth.username",value)

  def shouldHavePassword(json: JValue, value: String) =
    hasValue(json,"basic_auth.password",value)

  def shouldHaveService(json: JValue, value: String) =
    hasValue(json,"oauth.service",value)

  def shouldHaveToken(json: JValue, value: String) =
    hasValue(json,"oauth.token",value)

  def shouldHaveVerifier(json: JValue, value: String) =
    hasValue(json,"oauth.verifier",value)

  def shouldHaveRedirectUri(json: JValue, value: String) =
    hasValue(json,"oauth.redirect_uri",value)

  def shouldHaveAccessToken(json: JValue, value: String) =
    hasValue(json,"oauth.access_token",value)

  def shouldHaveCode(json: JValue, value: String) =
    hasValue(json,"oauth.code",value)


  def hasValue(json: JValue, key: String, value: String) =
    (json \ key) should be(JString(value))
}
