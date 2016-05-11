package io.corbel.sdk.notifications

import com.ning.http.client.Response
import dispatch._
import io.corbel.sdk.auth.AuthenticationProvider._
import io.corbel.sdk.auth.{AutomaticAuthentication, UsesAuthentication}
import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.error.ApiError._
import io.corbel.sdk.http.CorbelHttpClient
import io.corbel.sdk.http.CorbelHttpClient._
import io.corbel.sdk.iam._
import io.corbel.sdk.notifications.NotificationsClient._
import org.json4s.DefaultFormats
import org.json4s.native.Serialization._

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class NotificationsClient(implicit val config: CorbelConfig) extends CorbelHttpClient with Notifications with UsesAuthentication {
  implicit val formats = DefaultFormats

  override def sendNotification(id: String, recipient: String, properties: Map[String, String])(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError, Unit]] = {
    val jsonProperties = write(properties)
    auth(token => {
      val req = (notifications / send).json.withAuth(token) << s"""{"notificationId":"$id","recipient": "$recipient", "properties": $jsonProperties }"""
      http(req.POST > response.eitherApiError).map(_.right.map(_ => {}))
    })
  }
  private def response = (response: Response) => response
}

object NotificationsClient {
  def apply()(implicit config: CorbelConfig) = new NotificationsClient()

  def withAutomaticAuthentication(providedClientCredentials: ClientCredentials, providedUserCredentials: Option[UserCredentials] = None, providedAuthenticationOptions: AuthenticationOptions = AuthenticationOptions.default, providedExecutionContext: ExecutionContext = ExecutionContext.global)
                                 (implicit config: CorbelConfig) =
    new NotificationsClient() with AutomaticAuthentication {
      override val clientCredentials: ClientCredentials = providedClientCredentials
      override val userCredentials: Option[UserCredentials] = providedUserCredentials
      override val authenticationOptions: AuthenticationOptions = providedAuthenticationOptions
      override val executionContext: ExecutionContext = providedExecutionContext
      override val authClient: AuthenticationClient = IamClient()
    }

  private val send = "v1.0/notification/send"
}