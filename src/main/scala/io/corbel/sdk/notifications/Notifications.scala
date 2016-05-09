package io.corbel.sdk.notifications

import io.corbel.sdk.auth.AuthenticationProvider._
import io.corbel.sdk.error.ApiError

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait Notifications {

  def sendNotification(id: String)(implicit authenticationProvider: AuthenticationProvider, ec: ExecutionContext): Future[Either[ApiError, Unit]]

}
