package io.corbel.sdk.iam

import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpRequest._
import org.mockserver.model.HttpResponse._
import org.mockserver.model.JsonSchemaBody._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class IamClientTest extends FlatSpec with Matchers with BeforeAndAfter with ScalaFutures with PatienceConfiguration {

  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  var mockServer: ClientAndServer = null
  implicit val config = CorbelConfig(
    iamBaseUri = "http://localhost:1080",
    resourceBaseUri = "http://localhost:1080"
  )
  val clientId = "123"
  val cleintSecret = "567"

  behavior of "authenticate"

  it should "make a correct token request" in {
    mockServer
      .when(authenticationRequest)
      .respond(
        response()
          .withStatusCode(201)
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              |{
              |  "accessToken": "valid-token",
              |  "expiresAt": 1385377605000,
              |  "refreshToken": "refresh-token"
              |}
            """.stripMargin)
      )

    val iam = IamClient()
    val clientCredentials = ClientCredentials(clientId, cleintSecret)
    val futureResponse = iam.authenticate(clientCredentials)

    whenReady(futureResponse) { response =>
      response should be(Right(AuthenticationResponse("valid-token", 1385377605000l, Some("refresh-token"))))
    }
  }

  it should "catch API errors" in {
    mockServer
      .when(authenticationRequest)
      .respond(
        response()
          .withStatusCode(401)
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              |{
              |  "error": "Unauthorized",
              |  "errorDescription": "Invalid credentials",
              |}
            """.stripMargin)
      )


    val iam = IamClient()
    val clientCredentials = ClientCredentials(clientId, cleintSecret)
    val futureResponse = iam.authenticate(clientCredentials)

    whenReady(futureResponse) { response =>
      response should be(Left(ApiError(401, Some("Unauthorized"), Some("Invalid credentials"))))
    }
  }

  def authenticationRequest = request()
    .withMethod("POST")
    .withPath("/v1.0/oauth/token")
    .withBody(jsonSchema(
      """
        |{
        | "type":"object", "properties":{
        |   "assertion": {"type": "string"},
        |   "grant_type": {"type": "string"}
        | }
        |}
      """.stripMargin))

  before {
    mockServer = startClientAndServer(1080)
  }

  after {
    mockServer.stop()
  }


}
