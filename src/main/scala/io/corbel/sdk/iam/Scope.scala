package io.corbel.sdk.iam

import org.json4s.JsonAST.JObject

/**
  * @author Alberto J. Rubio (alberto.rubio@devialab.com)
  */
case class Scope(
                  id: Option[String] = None,
                  `type`: Option[String] = None,
                  audience: Option[String] = None,
                  rules: Option[Seq[ScopeRules]] = None,
                  scopes: Option[Seq[String]] = None,
                  parameters: Option[JObject] = None
               )

case class ScopeRules(mediaTypes: Seq[String], methods: Seq[String], `type`: String, uri: String, tokenType: Option[String])
