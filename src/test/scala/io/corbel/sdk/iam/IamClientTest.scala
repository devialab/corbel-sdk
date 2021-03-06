package io.corbel.sdk.iam

import io.corbel.sdk.auth.AuthenticationProvider
import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import org.json4s.JsonAST.JObject
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpRequest._
import org.mockserver.model.HttpResponse._
import org.mockserver.model.RegexBody
import org.mockserver.model.JsonSchemaBody._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.duration._
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
    resourceBaseUri = "http://localhost:1080",
    notificationsBaseUri = "http://localhost:1080",
    defaultTokenExpiration = 300000 millis
  )
  val clientId = "123"
  val cleintSecret = "567"
  val testToken = "AAAABBBCCCC"

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

  behavior of "getScope"

  it should "make request to GET scope by Id" in {
    val scopeId = "myScope"
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

    implicit val auth = AuthenticationProvider(testToken)
    val iam = IamClient()
    val futureResponse = iam.getScope(scopeId)

    whenReady(futureResponse) { response =>
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

  behavior of "createUser"

  it should "make request to POST user" in {
    mockServer
      .when(createUserRequest)
      .respond(
        response()
          .withStatusCode(201)
          .withHeader("Content-Type", "application/json")
          .withHeader("Location", "http://iam/v1.0/user/1234")
      )

    implicit val auth = AuthenticationProvider(testToken)
    val iam = IamClient()
    val futureResponse = iam.createUser(User(
      username = Some("test"),
      email = Some("test@test.com"),
      password = Some("password")
    ))

    whenReady(futureResponse) { response =>
      response should be(Right("1234"))
    }

  }

  behavior of "getUser"

  it should "make request to GET user by Id" in {
    val userId = "123"
    mockServer
      .when(getUserRequest(userId))
      .respond(
        response()
          .withStatusCode(200)
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              |{
              |  "id": "fdad3eaa19ddec84b397a9e9a2312262",
              |  "domain": "silkroad",
              |  "email": "alexander.deleon@bqreaders.com",
              |  "username": "alexander.deleon@bqreaders.com",
              |  "firstName": "alex",
              |  "scopes": [],
              |  "groups": ["test-group"],
              |  "properties": {}
              |}
            """.stripMargin)
      )

    implicit val auth = AuthenticationProvider(testToken)
    val iam = IamClient()
    val futureResponse = iam.getUserById(userId)

    whenReady(futureResponse) { response =>
      response should be(Right(User(
        id = Some("fdad3eaa19ddec84b397a9e9a2312262"),
        domain = Some("silkroad"),
        email = Some("alexander.deleon@bqreaders.com"),
        firstName = Some("alex"),
        username = Some("alexander.deleon@bqreaders.com"),
        scopes = Some(Seq.empty),
        groups = Some(Seq("test-group")),
        properties = Some(JObject())
      )))
    }
  }

  behavior of "getUserDevices"

  it should "make request to GET user devices by userId" in {
    val userId = "123"
    mockServer
      .when(getUserDevices(userId))
      .respond(
        response()
          .withStatusCode(200)
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              |[{
              |  "id": "1234",
              |  "notificationUri": "notificationUri-1"
              |},
              |{
              |  "id": "5678",
              |  "notificationUri": "notificationUri-2"
              |}]
            """.stripMargin)
      )

    implicit val auth = AuthenticationProvider(testToken)
    val iam = IamClient()
    val futureResponse = iam.getUserDevices(userId)

    whenReady(futureResponse) { response =>
      response should be(Right(Seq(
        Device(id = Some("1234"), notificationUri = Some("notificationUri-1")),
        Device(id = Some("5678"), notificationUri = Some("notificationUri-2"))
      )))
    }
  }

  behavior of "getUserId"

  it should "make request to GET userId by username" in {
    val username = "john.doe"
    mockServer
      .when(getUserIdByUsernameRequest(username))
      .respond(
        response()
          .withStatusCode(200)
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
              |{
              |  "id": "fdad3eaa19ddec84b397a9e9a2312262"
              |}
            """.stripMargin)
      )

    implicit val auth = AuthenticationProvider(testToken)
    val iam = IamClient()
    val futureResponse = iam.getUserIdByUsername(username)

    whenReady(futureResponse) { response =>
      response should be(Right(User(
        id = Some("fdad3eaa19ddec84b397a9e9a2312262")
      )))
    }
  }

  behavior of "createUserGroup"

  it should "make request to POST user group" in {
    mockServer
      .when(createUserGroupRequest)
      .respond(
        response()
          .withStatusCode(201)
          .withHeader("Content-Type", "application/json")
          .withHeader("Location", "http://iam/v1.0/group/123")
      )

    implicit val auth = AuthenticationProvider(testToken)
    val iam = IamClient()
    val futureResponse = iam.createGroup(Group(
      name = Some("test-group"),
      domain = Some("test"),
      scopes = Some(Seq("test-scope"))
    ))

    whenReady(futureResponse) { response =>
      response should be(Right("123"))
    }

  }

  behavior of "deleteGroupToUser"

  it should "make request to DELETE userId in groupId" in {
    val userId = "12312412412"
    val groupId = "1243151531235"
    mockServer
      .when(deleteGroupToUserRequest(userId, groupId))
      .respond(
        response()
          .withStatusCode(204)
          .withHeader("Content-Type", "application/json")
      )

    implicit val auth = AuthenticationProvider(testToken)
    val iam = IamClient()
    val futureResponse = iam.deleteGroupToUser(userId, groupId)

    whenReady(futureResponse) { response =>
      response should be(Right())
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

  def createUserRequest = request()
    .withMethod("POST")
    .withPath("/v1.0/user")
    .withHeader("Authorization", s"Bearer $testToken")
    .withBody(jsonSchema(
      """
        |{
        |  "username": "test",
        |  "email": "test@test.com",
        |  "password": "password"
        |}
      """.stripMargin))

  def getUserRequest(userId: String) = request()
      .withMethod("GET")
      .withPath(s"/v1.0/user/$userId")
      .withHeader("Authorization", s"Bearer $testToken")

  def getUserIdByUsernameRequest(username: String) = request()
    .withMethod("GET")
    .withPath(s"/v1.0/username/$username")
    .withHeader("Authorization", s"Bearer $testToken")

  def getUserDevices(userId: String) = request()
    .withMethod("GET")
    .withPath(s"/v1.0/user/$userId/device")
    .withHeader("Authorization", s"Bearer $testToken")

  def createUserGroupRequest = request()
      .withMethod("POST")
      .withPath("/v1.0/group")
      .withHeader("Authorization", s"Bearer $testToken")
      .withBody(jsonSchema(
        """
          |{
          | "type":"object", "properties":{
          |   "assertion": {"type": "string"},
          |   "grant_type": {"type": "string"}
          | }
          |}
        """.stripMargin))

  def deleteGroupToUserRequest(userId: String, groupId: String) = request()
    .withMethod("DELETE")
    .withPath(s"/v1.0/user/$userId/group/$groupId")
    .withHeader("Authorization", s"Bearer $testToken")

  before {
    mockServer = startClientAndServer(1080)
  }

  after {
    mockServer.stop()
  }

}
