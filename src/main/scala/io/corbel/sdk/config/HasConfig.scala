package io.corbel.sdk.config

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait HasConfig {
  implicit val config: CorbelConfig
}
