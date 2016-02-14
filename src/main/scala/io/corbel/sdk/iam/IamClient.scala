package io.corbel.sdk.iam

import com.ning.http.client.Response
import dispatch._
import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.error.ApiError._
import io.corbel.sdk.http.CorbelHttpClient
import io.corbel.sdk.iam.IamClient._
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._
import pdi.jwt.{Jwt, JwtAlgorithm}

import _root_.scala.concurrent.{ExecutionContext, Future}

/**
  * Iam interface implementation
  *
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class IamClient(implicit config: CorbelConfig) extends CorbelHttpClient with Iam {

  implicit val formats = DefaultFormats

  override def authenticationRefresh(clientCredentials: ClientCredentials, refreshToken: String, authenticationOptions: AuthenticationOptions)
                                    (implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]] = {
    val claims = Claims()
      .addClientCredentials(clientCredentials)
      .addRefreshToken(refreshToken)
      .addOptions(authenticationOptions)

    doAuthenticate(buildAssertion(claims, clientCredentials.secret))
  }

  override def authenticate(clientCredentials: ClientCredentials, userCredentials: Option[UserCredentials], authenticationOptions: AuthenticationOptions)
                           (implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]] = {
    val claims = Claims()
      .addClientCredentials(clientCredentials)
    for (userCredentials <- userCredentials) {
      claims.addUserCredentials(userCredentials)
    }
    claims.addOptions(authenticationOptions)

    doAuthenticate(buildAssertion(claims, clientCredentials.secret))
  }

  private def doAuthenticate(assertionParam: String)(implicit ec: ExecutionContext): Future[Either[ApiError,AuthenticationResponse]] = {
    val req = (iam / `oauth/token`).jsonContentType <<
      compact(render((assertion -> assertionParam) ~ (grant_type -> `jwt-bearer`)))
    http(req > asAuthenticationResponse.eitherApiError)
  }

  private def buildAssertion(claims: Claims, secret: String) = Jwt.encode(claims.toJson, key = secret, algorithm = JwtAlgorithm.HmacSHA256)

  private val asAuthenticationResponse = (response: Response) =>
    read[AuthenticationResponse](response.getResponseBodyAsStream)

}


object IamClient {

  def apply()(implicit config: CorbelConfig) = new IamClient()

  //endpoints
  private val `oauth/token` = "v1.0/oauth/token"

  private val assertion = "assertion"
  private val grant_type = "grant_type"
  private val `jwt-bearer` = "urn:ietf:params:oauth:grant-type:jwt-bearer"
}

