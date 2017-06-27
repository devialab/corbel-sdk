package io.corbel.sdk.auth

import grizzled.slf4j.Logging
import io.corbel.sdk.api.RequestParams
import io.corbel.sdk.auth.AuthenticationProvider.AuthenticationProvider
import io.corbel.sdk.config.{HasConfig, CorbelConfig}
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.iam._
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}

import scala.concurrent.{Await, Future, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class AutomaticAuthenticationTest extends FlatSpec with Matchers with MockFactory with ScalaFutures with Logging {

  val testToken = "TTTT"
  val testRefreshToken = "XXXXX"
  val testClientCredentials = ClientCredentials("_test_client", "_test_secret")
  val iamMock = mock[Iam]
  val testConfig = CorbelConfig("http://localhost:1", "http://localhost:2", "http://localhost:3")

  "AutomaticAuthenticationProvider" should "request a new token from IAM" in {
    (iamMock.authenticate(_: ClientCredentials, _: Option[UserCredentials], _: AuthenticationOptions)(_: ExecutionContext)).expects(testClientCredentials, None, AuthenticationOptions.default, *).returning(Future.successful(Right(AuthenticationResponse(testToken, System.currentTimeMillis(), Some(testRefreshToken)))))

    val autoLogin = new AutomaticAuthenticationProvider(testClientCredentials, None, AuthenticationOptions.default, iamMock)
    whenReady(autoLogin.apply()) { token =>
      token should be(testToken)
    }
  }

  behavior of "AutomaticAuthentication trait"

  it should "call authenticate and authentication refresh" in {
    val iam = new IamSub with AutomaticAuthentication {
      override val authenticationOptions: AuthenticationOptions = AuthenticationOptions.default
      override val userCredentials: Option[UserCredentials] = Some(BasicUserCredentials("user","pass"))
      override implicit val executionContext: ExecutionContext = ExecutionContext.global
      override val clientCredentials: ClientCredentials = testClientCredentials
      override val authClient: AuthenticationClient = this
    }
    iam.authenticateStub.when(testClientCredentials, *, *, *).once().returning(Future.successful(Right(AuthenticationResponse(testToken, 0, Some(testRefreshToken)))))
    iam.authenticationRefreshStub.when(testClientCredentials, testRefreshToken, *, *).returning(Future.successful(Right(AuthenticationResponse(testToken, 0, Some(testRefreshToken)))))

    iam.createUserStub.when(*, *, *).once().onCall((_,token,_) => {
      Future.successful(Left(ApiError(401)))
    })

    iam.createUserStub.when(*, *, *).once().returning(Future.successful(Right("UserCreated")))

    whenReady(iam.createUser(User())) { response =>
      response should be(Right("UserCreated"))
    }

    iam.createGroupStub.when(*, *, *).once().onCall((_,token,_) => {
      Future.successful(Left(ApiError(401)))
    })

    iam.createGroupStub.when(*, *, *).once().returning(Future.successful(Right("GroupCreated")))

    whenReady(iam.createGroup(Group())) { response =>
      response should be(Right("GroupCreated"))
    }
  }

  it should "retry when received authentication error (401)" in {
    val iam = new IamSub with AutomaticAuthentication {
      override val authenticationOptions: AuthenticationOptions = AuthenticationOptions.default
      override val userCredentials: Option[UserCredentials] = Some(BasicUserCredentials("user", "passwd"))
      override implicit val executionContext: ExecutionContext = ExecutionContext.global
      override val clientCredentials: ClientCredentials = testClientCredentials
      override val authClient: AuthenticationClient = this
    }

    iam.authenticateStub.when(*, *, *, *).returning(Future.successful(Right(AuthenticationResponse(testToken, 0l, Some(testRefreshToken)))))
    iam.authenticationRefreshStub.when(*, testRefreshToken, *, *).returning(Future.successful(Right(AuthenticationResponse(testToken, 0l, Some(testRefreshToken)))))

    val validUserAuth = (u: User, token: String, ec: ExecutionContext) => {
      info(s"Auth with token: $token")
      token == testToken
    }

    val validGroupAuth = (g: Group, token: String, ec: ExecutionContext) => {
      info(s"Auth with token: $token")
      token == testToken
    }

    iam.createUserStub.when(where(validUserAuth)).once().returning(Future.successful(Left(ApiError(401))))
    iam.createUserStub.when(where(validUserAuth)).once().returning(Future.successful(Right("UserCreated")))

    whenReady(iam.createUser(User())) { response =>
      response should be(Right("UserCreated"))
    }

    iam.createGroupStub.when(where(validGroupAuth)).once().returning(Future.successful(Left(ApiError(401))))
    iam.createGroupStub.when(where(validGroupAuth)).once().returning(Future.successful(Right("GroupCreated")))

    whenReady(iam.createGroup(Group())) { response =>
      response should be(Right("GroupCreated"))
    }
  }

  it should "should not loop indefinitely on access forbidden" in {
    val iam = new IamSub with AutomaticAuthentication {
      override val authenticationOptions: AuthenticationOptions = AuthenticationOptions.default
      override val userCredentials: Option[UserCredentials] = None
      override implicit val executionContext: ExecutionContext = ExecutionContext.global
      override val clientCredentials: ClientCredentials = testClientCredentials
      override val authClient: AuthenticationClient = this
    }

    iam.authenticateStub.when(*, *, *, *).returning(Future.successful(Right(AuthenticationResponse(testToken, 0l, None))))
    iam.authenticationRefreshStub.when(*, testRefreshToken, *, *).returning(Future.successful(Right(AuthenticationResponse(testToken, 0l, None))))

    val validUserAuth = (u: User, token: String, ec: ExecutionContext) => {
      info(s"Auth with token: $token")
      token == testToken
    }

    val validGroupAuth = (g: Group, token: String, ec: ExecutionContext) => {
      info(s"Auth with token: $token")
      token == testToken
    }

    iam.createUserStub.when(where(validUserAuth)).once().returning(Future.successful(Left(ApiError(401))))
    iam.createUserStub.when(where(validUserAuth)).once().returning(Future.successful(Left(ApiError(401))))

    whenReady(iam.createUser(User())) { response =>
      response should be(Left(ApiError(401)))
    }

    iam.createGroupStub.when(where(validGroupAuth)).once().returning(Future.successful(Left(ApiError(401))))
    iam.createGroupStub.when(where(validGroupAuth)).once().returning(Future.successful(Left(ApiError(401))))

    whenReady(iam.createGroup(Group())) { response =>
      response should be(Left(ApiError(401)))
    }
  }


  class IamSub extends Iam with UsesAuthentication with HasConfig {
    val authenticateStub = stubFunction[ClientCredentials, Option[UserCredentials], AuthenticationOptions, ExecutionContext, Future[Either[ApiError,AuthenticationResponse]]]
    val authenticationRefreshStub = stubFunction[ClientCredentials, String, AuthenticationOptions, ExecutionContext, Future[Either[ApiError, AuthenticationResponse]]]
    val createUserStub = stubFunction[User, String, ExecutionContext, Future[Either[ApiError, String]]]
    val createGroupStub = stubFunction[Group, String, ExecutionContext, Future[Either[ApiError, String]]]
    override implicit val config: CorbelConfig = testConfig

    override def addGroupsToUser(userId: String, groups: Iterable[String])(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Unit]] = ???

    override def deleteGroupToUser(userId: String, groupId: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Unit]] = ???

    override def createUser(user: User)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, String]] = auth { token => createUserStub(user, token, ec) }

    override def getUserById(id: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, User]] = ???

    override def getUserIdByUsername(username: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, User]] = ???

    override def getUser(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, User]] = ???

    override def getUserDevices(userId: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Seq[Device]]] = ???

    override def updateUser(user: User)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Unit]] = ???

    override def findUsers(params: RequestParams)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Seq[User]]] = ???

    override def authenticate(clientCredentials: ClientCredentials, userCredentials: Option[UserCredentials], authenticationOptions: AuthenticationOptions)(implicit ec: ExecutionContext): Future[Either[ApiError, AuthenticationResponse]] = authenticateStub(clientCredentials, userCredentials, authenticationOptions, ec)

    override def authenticationRefresh(clientCredentials: ClientCredentials, refreshToken: String, authenticationOptions: AuthenticationOptions)(implicit ec: ExecutionContext): Future[Either[ApiError, AuthenticationResponse]] = authenticationRefreshStub(clientCredentials, refreshToken, authenticationOptions, ec)

    override def createGroup(group: Group)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, String]] = auth { token => createGroupStub(group, token, ec) }

    override def getScope(id: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Scope]] = ???
  }

}

