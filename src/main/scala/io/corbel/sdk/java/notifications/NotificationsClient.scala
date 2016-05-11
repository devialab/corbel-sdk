package io.corbel.sdk.java.notifications

import java.util
import java.util.Optional
import java.util.concurrent.{CompletionStage, ForkJoinPool}

import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.iam._
import io.corbel.sdk.notifications._

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._

/**
  * @author Ismael Madirolas (ismael.madirolas@devialab.com)
  */
class NotificationsClient(clientCredentials: ClientCredentials, userCredentials: Optional[UserCredentials], authenticationOptions: Optional[AuthenticationOptions], corbelConfig: CorbelConfig) extends Notifications {

  private implicit val executionContext = ExecutionContext.fromExecutor(new ForkJoinPool())

  private val delegate  = NotificationsClient.withAutomaticAuthentication(clientCredentials, userCredentials.asScala, authenticationOptions.orElse(AuthenticationOptions.default), executionContext)(corbelConfig)

  override def sendNotification(id: String, recipient: String, properties: util.Map[String, String]): CompletionStage[Either[ApiError, Unit]] = delegate.sendNotification(id, recipient, properties.asScala.toMap).toJava
}
