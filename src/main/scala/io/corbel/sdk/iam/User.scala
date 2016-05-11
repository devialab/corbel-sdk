package io.corbel.sdk.iam

import org.json4s.JsonAST.JObject

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class User(
                 id: Option[String] = None,
                 domain: Option[String] = None,
                 username: Option[String] = None,
                 email: Option[String] = None,
                 firstName: Option[String] = None,
                 lastName: Option[String] = None,
                 scopes: Option[Seq[String]] = None,
                 properties: Option[JObject] = None
               )
