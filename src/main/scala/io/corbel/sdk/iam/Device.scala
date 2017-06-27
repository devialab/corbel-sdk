package io.corbel.sdk.iam

/**
  * @author Alberto J. Rubio (alberto.rubio@devialab.com)
  */
case class Device(
                   id: Option[String] = None,
                   notificationUri: Option[String] = None,
                   name: Option[String] = None,
                   `type`: Option[String] = None,
                   notificationEnabled: Option[Boolean] = None,
                   firstConnection: Option[String] = None,
                   lastConnection: Option[String] = None
                 )