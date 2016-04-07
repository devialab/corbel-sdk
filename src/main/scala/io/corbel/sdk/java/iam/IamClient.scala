package io.corbel.sdk.java.iam

import java.util
import java.util.Optional
import java.util.concurrent.{ForkJoinPool, CompletionStage, Executors}

import io.corbel.sdk.auth.{AutomaticAuthentication, AuthenticationProvider}
import io.corbel.sdk.config.CorbelConfig
import io.corbel.sdk.error.ApiError
import io.corbel.sdk.iam._

import scala.concurrent.ExecutionContext

import scala.collection.JavaConversions._
import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class IamClient(clientCredentials: ClientCredentials, userCredentials: Optional[UserCredentials], authenticationOptions: Optional[AuthenticationOptions], corbelConfig: CorbelConfig) extends Iam {

  private implicit val executionContext = ExecutionContext.fromExecutor(new ForkJoinPool())
  private val delegate  = IamClient.withAutomaticAuthentication(clientCredentials, userCredentials.asScala, authenticationOptions.orElse(AuthenticationOptions.default), executionContext)(corbelConfig)

  override def addGroupsToUser(userId: String, groups: util.Collection[String]): CompletionStage[Either[ApiError, Unit]] = delegate.addGroupsToUser(userId, groups).toJava

  override def getUser(): CompletionStage[Either[ApiError, User]] = delegate.getUser.toJava

  override def getUserById(id: String): CompletionStage[Either[ApiError, User]] = delegate.getUserbyId(id).toJava

  override def authenticate(clientCredentials: ClientCredentials, userCredentials: Optional[UserCredentials], authenticationOptions: AuthenticationOptions): CompletionStage[Either[ApiError, AuthenticationResponse]] =
    delegate.authenticate(clientCredentials, userCredentials.asScala, authenticationOptions).toJava

  override def authenticationRefresh(clientCredentials: ClientCredentials, refreshToken: String, authenticationOptions: AuthenticationOptions): CompletionStage[Either[ApiError, AuthenticationResponse]] =
    delegate.authenticationRefresh(clientCredentials, refreshToken, authenticationOptions).toJava

  override def createGroup(group: Group): CompletionStage[Either[ApiError, String]] = delegate.createGroup(group).toJava

  override def getScope(id: String): CompletionStage[Either[ApiError, Scope]] = delegate.getScope(id).toJava
}
