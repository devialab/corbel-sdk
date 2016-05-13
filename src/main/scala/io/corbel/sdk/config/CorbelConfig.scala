package io.corbel.sdk.config

import scala.concurrent.duration._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class CorbelConfig(
                         iamBaseUri: String,
                         resourceBaseUri: String,
                         notificationsBaseUri: String,
                         simultaneousRequestsLimit: Int = 25,
                         defaultTokenExpiration: Duration = 59 minutes
                       )
