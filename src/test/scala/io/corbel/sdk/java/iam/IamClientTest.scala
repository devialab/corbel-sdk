package io.corbel.sdk.java.iam

import java.util.Optional

import io.corbel.sdk.auth.AuthenticationProvider
import io.corbel.sdk.config.CorbelConfig
import org.mockserver.model.RegexBody
import scala.compat.java8.FutureConverters._
import io.corbel.sdk.iam.{Scope, ClientCredentials}
import org.json4s.JsonAST.JObject
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpRequest._
import org.mockserver.model.HttpResponse._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.duration._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class IamClientTest extends FlatSpec with Matchers with BeforeAndAfter with ScalaFutures with PatienceConfiguration {

  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  var mockServer: ClientAndServer = null
  implicit val config = CorbelConfig(
    iamBaseUri = "http://localhost:1080",
    resourceBaseUri = "http://localhost:1080",
    defaultTokenExpiration = 300000 millis
  )
  val clientId = "123"
  val cleintSecret = "567"
  val testToken = "AAAABBBCCCC"


  behavior of "getScope"

  it should "make request to GET scope by Id" in {
    val scopeId = "myScope"

    mockServer
      .when(authenticationRequest)
      .respond(
        response()
          .withStatusCode(201)
          .withHeader("Content-Type", "application/json")
          .withBody(
            s"""
              |{
              |  "accessToken": "$testToken",
              |  "expiresAt": 1385377605000,
              |  "refreshToken": "refresh-token"
              |}
            """.stripMargin)
      )

    mockServer
      .when(getScopeRequest(scopeId))
      .respond(
        response()
          .withStatusCode(200)
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              |{
              |  "id": "scopeId",
              |  "type": "scopeType",
              |  "audience": "scopeAudience",
              |  "rules": [],
              |  "scopes": [],
              |  "parameters": {}
              |}
            """.stripMargin)
      )

    val iam = new IamClient(ClientCredentials(clientId, cleintSecret), Optional.empty(), Optional.empty(), config)
    val futureResponse = iam.getScope(scopeId)

    whenReady(futureResponse.toScala) { response =>
      response should be(Right(Scope(
        id = Some("scopeId"),
        `type` = Some("scopeType"),
        audience = Some("scopeAudience"),
        rules = Some(Seq.empty),
        scopes = Some(Seq.empty),
        parameters = Some(JObject())
      )))
    }
  }


  /* ---------------- helper methods ---------------- */
  def authenticationRequest = request()
    .withMethod("POST")
    .withPath("/v1.0/oauth/token")
    .withHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
    .withBody(RegexBody.regex("assertion=.+&grant_type=.+"))

  def getScopeRequest(scopeId: String) = request()
    .withMethod("GET")
    .withPath(s"/v1.0/scope/$scopeId")
    .withHeader("Authorization", s"Bearer $testToken")


  before {
    mockServer = startClientAndServer(1080)
  }

  after {
    mockServer.stop()
  }

}
