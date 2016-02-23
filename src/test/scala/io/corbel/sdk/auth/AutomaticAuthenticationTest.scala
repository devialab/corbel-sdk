package io.corbel.sdk.auth

import io.corbel.sdk.auth.AuthenticationProvider.AuthenticationProvider
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.iam._
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}

import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global


/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class AutomaticAuthenticationTest extends FlatSpec with Matchers with MockFactory with ScalaFutures {

  val testToken = "TTTT"
  val testRefreshToken = "XXXXX"
  val testClientCredentials = ClientCredentials("_test_client", "_test_secret")
  val iamMock = mock[Iam]

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
      override val userCredentials: Option[UserCredentials] = None
      override implicit val executionContext: ExecutionContext = ExecutionContext.global
      override val clientCredentials: ClientCredentials = testClientCredentials
    }
    iam.authenticateStub.when(testClientCredentials, None, *, *).once().returning(Future.successful(Right(AuthenticationResponse(testToken, 0, Some(testRefreshToken)))))
    iam.authenticationRefreshStub.when(testClientCredentials, testRefreshToken, *, *).returning(Future.successful(Right(AuthenticationResponse(testToken, 0, Some(testRefreshToken)))))
    iam.createGroupStub.when(*, *, *).once().onCall((_,provider,_) => {
      provider.apply()
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
      override val userCredentials: Option[UserCredentials] = None
      override implicit val executionContext: ExecutionContext = ExecutionContext.global
      override val clientCredentials: ClientCredentials = testClientCredentials
    }
    iam.createGroupStub.when(*, *, *).once().returning(Future.successful(Left(ApiError(401))))
    iam.createGroupStub.when(*, *, *).once().returning(Future.successful(Right("GroupCreated")))

    whenReady(iam.createGroup(Group())) { response =>
      response should be(Right("GroupCreated"))
    }
  }


  class IamSub extends Iam {

    val authenticateStub = stubFunction[ClientCredentials, Option[UserCredentials], AuthenticationOptions, ExecutionContext, Future[Either[ApiError,AuthenticationResponse]]]
    val authenticationRefreshStub = stubFunction[ClientCredentials, String, AuthenticationOptions, ExecutionContext, Future[Either[ApiError, AuthenticationResponse]]]
    val createGroupStub = stubFunction[Group, AuthenticationProvider, ExecutionContext, Future[Either[ApiError, String]]]

    override def addGroupsToUser(userId: String, groups: Iterable[String])(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Unit]] = ???

    override def getUserbyId(id: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, User]] = ???

    override def getUser(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, User]] = ???

    override def authenticate(clientCredentials: ClientCredentials, userCredentials: Option[UserCredentials], authenticationOptions: AuthenticationOptions)(implicit ec: ExecutionContext): Future[Either[ApiError, AuthenticationResponse]] = authenticateStub(clientCredentials, userCredentials, authenticationOptions, ec)

    override def authenticationRefresh(clientCredentials: ClientCredentials, refreshToken: String, authenticationOptions: AuthenticationOptions)(implicit ec: ExecutionContext): Future[Either[ApiError, AuthenticationResponse]] = authenticationRefreshStub(clientCredentials, refreshToken, authenticationOptions, ec)

    override def createGroup(group: Group)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, String]] = createGroupStub(group, authenticationProvider, ec)
  }

}

