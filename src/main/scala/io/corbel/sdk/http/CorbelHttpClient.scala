package io.corbel.sdk.http

import io.corbel.sdk.AuthenticationProvider
import io.corbel.sdk.config.CorbelConfig
import dispatch._
import org.jboss.netty.handler.codec.http.HttpHeaders

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

  implicit class CorbelRequest(val req: Req) extends AnyVal {
    def json = req.jsonContentType.acceptJson
    def formUrlEncoded = req.setContentType("application/x-www-form-urlencoded", "UTF-8")
    def withAuth(implicit authenticationProvider: AuthenticationProvider): Req = withAuth(authenticationProvider())
    def withAuth(token: String): Req = req.setHeader(HttpHeaders.Names.AUTHORIZATION, s"Bearer $token")
    def jsonContentType: Req = req.setContentType("application/json", "UTF-8")
    def acceptJson: Req = req.setHeader(HttpHeaders.Names.ACCEPT, "application/json")
  }
}