package io.corbel.sdk.notifications

import io.corbel.sdk.auth.AuthenticationProvider._
import io.corbel.sdk.error.ApiError

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait Notifications {

  def sendNotification(id: String, recipient: String, properties: mutable.Map[String, String])(implicit authenticationProvider: AuthenticationProvider = null, ec: ExecutionContext): Future[Either[ApiError, Unit]]

}
