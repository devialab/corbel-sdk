package io.corbel.sdk.iam

import com.ning.http.client.Response
import dispatch._
import io.corbel.sdk.api._
import io.corbel.sdk.auth.AuthenticationProvider._
import io.corbel.sdk.auth.{AutomaticAuthentication, UsesAuthentication}
import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.error.ApiError._
import io.corbel.sdk.http.CorbelHttpClient
import io.corbel.sdk.iam.Claims.RefreshToken
import io.corbel.sdk.iam.IamClient._
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.json4s.DefaultFormats
import org.json4s.native.Serialization._
import pdi.jwt.{Jwt, JwtAlgorithm}
import CorbelHttpClient._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

/**
  * Iam interface implementation
  *
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class IamClient(implicit val config: CorbelConfig) extends CorbelHttpClient with Iam with ApiImplicits with UsesAuthentication {

  implicit val formats = DefaultFormats

  override def authenticationRefresh(clientCredentials: ClientCredentials, refreshToken: String, authenticationOptions: AuthenticationOptions)
                                    (implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]] = {
    val claims = Claims.default + clientCredentials + RefreshToken(refreshToken) + authenticationOptions

    doAuthenticate(buildAssertion(claims, clientCredentials.secret))
  }

  override def authenticate(clientCredentials: ClientCredentials, userCredentials: Option[UserCredentials], authenticationOptions: AuthenticationOptions)
                           (implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]] = {
    var claims = Claims.default + clientCredentials + authenticationOptions

    for (userCredentials <- userCredentials) {
      claims += userCredentials
    }

    doAuthenticate(buildAssertion(claims, clientCredentials.secret))
  }

  override def getScope(id: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError,Scope]] =
    auth(token => {
      val req = (iam / `scope/{id}`(id)).json.withAuth(token)
      http(req > as[Scope].eitherApiError)
    })

  override def createUser(entity: User)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, String]] =
    auth(token => {
      val req = (iam / user).json.withAuth(token) << write(entity)
      http(req > response.eitherApiError).map(resp => resp.right.map {
        _.getHeader(HttpHeaders.Names.LOCATION) match {
          case UserId(id) => id
          case loc => throw new IllegalStateException(s"Expecting correct user URI in Location header. I got $loc")
        }
      })
    })

  override def getUser(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError,User]] =
    auth(token => {
      val req = (iam / `user/me`).json.withAuth(token)
      http(req > as[User].eitherApiError)
    })

  override def getUserById(id: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError,User]] =
    auth(token => {
      val req = (iam / `user/{id}`(id)).json.withAuth(token)
      http(req > as[User].eitherApiError)
    })

  override def getUserIdByUsername(username: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError,User]] =
    auth(token => {
      val req = (iam / `username/{name}`(username)).json.withAuth(token)
      http(req > as[User].eitherApiError)
    })

  override def getUserDevices(userId: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Seq[Device]]] =
    auth(token => {
      var params = RequestParams(None, 0, 30, Option(SortParams(Direction.DESC, "lastConnection")))
      val req = (iam / `user/{id}/device`(userId)).json.withAuth(token) <<? params
      http(req > as[Seq[Device]].eitherApiError)
    })

  override def addGroupsToUser(userId: String, groups: Iterable[String])(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Unit]] =
    auth(token => {
      val req = (iam / `user/{id}/group`(userId)).json.withAuth(token) << write(groups)
      http(req.PUT > response.eitherApiError).map(_.right.map(_ => {}))
    })

  override def deleteGroupToUser(userId: String, groupId: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Unit]] =
    auth(token => {
      val req = (iam / `user/{id}/group/{groupId}`(userId, groupId)).json.withAuth(token)
      http(req.DELETE > response.eitherApiError).map(_.right.map(_ => {}))
    })


  override def updateUser(user: User)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): dispatch.Future[Either[ApiError, Unit]] = user.id match {
    case Some(userId) => auth(token => {
      val req = (iam / `user/{id}`(userId)).json.withAuth(token) << write(user.copy(id = None))
      http(req.PUT > response.eitherApiError).map(_.right.map(_ => {}))
    })
    case None => Future.failed(new IllegalArgumentException("User must have an id"))
  }

  override def findUsers(params: RequestParams)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): dispatch.Future[Either[ApiError, Seq[User]]] = {
    auth(token => {
      val req = (iam / user).json.withAuth(token) <<? params
      http(req > as[Seq[User]].eitherApiError)
    })
  }

  override def createGroup(userGroup: Group)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, String]] =
    auth(token => {
      val req = (iam / group).json.withAuth(token) << write(userGroup)
      http(req > response.eitherApiError).map(resp => resp.right.map {
        _.getHeader(HttpHeaders.Names.LOCATION) match {
          case GroupId(id) => id
          case loc => throw new IllegalStateException(s"Expecting correct group URI in Location header. I got $loc")
        }
      })
    })

  private def doAuthenticate(assertionParam: String)(implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]] = {
    val req = (iam / `oauth/token`).formUrlEncoded.acceptJson << Seq((assertion, assertionParam),(grant_type, `jwt-bearer`))
    http(req > as[AuthenticationResponse].eitherApiError)
  }

  private def buildAssertion(claims: Claims, secret: String) = Jwt.encode(claims.toJson, key = secret, algorithm = JwtAlgorithm.HS256)

  private def as[T](implicit ct: Manifest[T]) = (response: Response) => read[T](response.getResponseBodyAsStream)
  private def response = (response: Response) => response

}


object IamClient {

  def apply()(implicit config: CorbelConfig) = new IamClient()

  def withAutomaticAuthentication(providedClientCredentials: ClientCredentials, providedUserCredentials: Option[UserCredentials] = None , providedAuthenticationOptions: AuthenticationOptions = AuthenticationOptions.default, providedExecutionContext: ExecutionContext = ExecutionContext.global)
                                 (implicit config: CorbelConfig) =
    new IamClient() with AutomaticAuthentication {
      override val clientCredentials: ClientCredentials = providedClientCredentials
      override val userCredentials: Option[UserCredentials] = providedUserCredentials
      override val authenticationOptions: AuthenticationOptions = providedAuthenticationOptions
      override val executionContext: ExecutionContext = providedExecutionContext
      override val authClient: AuthenticationClient = this
    }

  //endpoints
  private val `oauth/token` = "v1.0/oauth/token"
  private val user = "v1.0/user"
  private val username = "v1.0/username"
  private def `user/{id}`(id: String) = s"$user/$id"
  private def `username/{name}`(name: String) = s"$username/$name"
  private val `user/me` = `user/{id}`("me")
  private def `user/{id}/device`(id:String) = s"v1.0/user/$id/device"
  private def `user/{id}/group`(id:String) = s"v1.0/user/$id/group"
  private def `user/{id}/group/{groupId}`(id:String, groupId: String) = s"v1.0/user/$id/group/$groupId"
  private val group = "v1.0/group"
  private def `scope/{id}`(id: String) = s"v1.0/scope/$id"

  private val assertion = "assertion"
  private val grant_type = "grant_type"
  private val `jwt-bearer` = "urn:ietf:params:oauth:grant-type:jwt-bearer"

  private val UserId = """.*/user/(\w*)$""".r
  private val GroupId = """.*/group/(\w*)$""".r
}

