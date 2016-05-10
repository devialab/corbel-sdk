package io.corbel.sdk.java.notifications

import java.util
import java.util.Optional
import java.util.concurrent.{CompletionStage, ForkJoinPool}

import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.iam.{AuthenticationOptions, ClientCredentials, IamClient, UserCredentials}
import io.corbel.sdk.notifications.NotificationsClient

import scala.concurrent.ExecutionContext
import scala.collection.JavaConversions._
import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._

/**
  * Created by ismael on 5/05/16.
  */
class NotificationsClient(clientCredentials: ClientCredentials, userCredentials: Optional[UserCredentials], authenticationOptions: Optional[AuthenticationOptions], corbelConfig: CorbelConfig) extends Notifications {

  private implicit val executionContext = ExecutionContext.fromExecutor(new ForkJoinPool())

  private val delegate  = NotificationsClient.withAutomaticAuthentication(clientCredentials, userCredentials.asScala, authenticationOptions.orElse(AuthenticationOptions.default), executionContext)(corbelConfig)

  override def sendNotification(id: String, recipient: String, properties: util.Map[String, String]): CompletionStage[Either[ApiError, Unit]] = delegate.sendNotification(id, recipient, properties).toJava
}
