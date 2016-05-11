package io.corbel.sdk.config

import scala.concurrent.duration._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class CorbelConfig(
                         iamBaseUri: String,
                         resourceBaseUri: String = null,
                         notificationsBaseUri: String = null,
                         defaultTokenExpiration: Duration = 59 minutes
                       )
