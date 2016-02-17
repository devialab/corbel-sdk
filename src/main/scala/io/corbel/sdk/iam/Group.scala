package io.corbel.sdk.iam

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class Group(
                      id: Option[String] = None,
                      name: Option[String] = None,
                      domain: Option[String] = None,
                      scopes: Option[Seq[String]] = None
                    )
