package io.corbel.sdk.http

import io.corbel.sdk.AuthenticationProvider
import io.corbel.sdk.config.CorbelConfig
import dispatch._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait CorbelHttpClient {
  import CorbelHttpClient._
  val http = Http()

  trait CorbelService {
    def /(path: String)(implicit config: CorbelConfig): CorbelRequest
  }
  object iam extends CorbelService {
    override def /(path: String)(implicit config: CorbelConfig): CorbelRequest = url(s"${config.iamBaseUri}/$path")
  }
  object resources extends CorbelService {
    override def /(path: String)(implicit config: CorbelConfig): CorbelRequest = url(s"${config.resourceBaseUri}/$path")
  }
}

object CorbelHttpClient {

  def jsonApi(req: CorbelRequest): Req = req.jsonContentType.acceptJson

  implicit class CorbelRequest(val req: Req) extends AnyVal {
    def withAuth(implicit authenticationProvider: AuthenticationProvider): Req = withAuth(authenticationProvider())
    def withAuth(token: String): Req = req.setHeader("Authorization", s"Bearer $token")
    def jsonContentType: Req = req.setContentType("application/json", "UTF-8")
    def acceptJson: Req = req.setHeader("Accept", "application/json")
  }
}