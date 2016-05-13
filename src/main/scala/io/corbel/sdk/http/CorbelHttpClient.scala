package io.corbel.sdk.http

import com.ning.http.client.extra.ThrottleRequestFilter
import io.corbel.sdk.config.{CorbelConfig, HasConfig}
import dispatch._
import org.jboss.netty.handler.codec.http.HttpHeaders

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait CorbelHttpClient extends HasConfig {

  import CorbelHttpClient._

  val http = Http().configure(_.addRequestFilter(new ThrottleRequestFilter(config.simultaneousRequestsLimit)))

  trait CorbelService {
    val urlBase: String
    def /(path: String)(implicit config: CorbelConfig): CorbelRequest = url(s"${urlBase}/$path")
  }
  object iam extends CorbelService {
    override val urlBase = config.iamBaseUri
  }
  object resources extends CorbelService {
    override val urlBase = config.resourceBaseUri
  }
  object notifications extends CorbelService {
    override val urlBase = config.notificationsBaseUri
  }
}

object CorbelHttpClient {

  implicit class CorbelRequest(val req: Req) extends AnyVal {
    def json = req.jsonContentType.acceptJson
    def formUrlEncoded = req.setContentType("application/x-www-form-urlencoded", "UTF-8")
    def withAuth(token: String): Req = req.setHeader(HttpHeaders.Names.AUTHORIZATION, s"Bearer $token")
    def jsonContentType: Req = req.setContentType("application/json", "UTF-8")
    def acceptJson: Req = req.setHeader(HttpHeaders.Names.ACCEPT, "application/json")
  }
}