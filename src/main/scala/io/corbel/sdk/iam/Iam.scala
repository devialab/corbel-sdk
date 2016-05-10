package io.corbel.sdk.iam

import io.corbel.sdk.api.{RequestParams, Query}
import io.corbel.sdk.auth.AuthenticationProvider._
import io.corbel.sdk.error.ApiError

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait Iam extends AuthenticationClient {

  /* ----------------- Scopes ---------------------- */

  def getScope(id: String)(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError,Scope]]

  /* ----------------- Users ---------------------- */

  def getUserById(id: String)(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError,User]]

  def getUser(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError,User]]

  def addGroupsToUser(userId: String, groups: Iterable[String])(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError,Unit]]

  def updateUser(user: User)(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError,Unit]]

  def findUsers(params: RequestParams)(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError,Seq[User]]]

  /* ----------------- Groups ---------------------- */

  def createGroup(group: Group)(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError,String]]
}


