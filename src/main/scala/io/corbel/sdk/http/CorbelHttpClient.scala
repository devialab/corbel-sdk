package io.corbel.sdk.http

import io.corbel.sdk.config.CorbelConfig
import dispatch._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
trait CorbelHttpClient {
  import CorbelHttpClient._
  val http = Http()

  trait CorbelService {
    def /(path: String)(implicit config: CorbelConfig): ReqWarpper
  }
  object iam extends CorbelService {
    override def /(path: String)(implicit config: CorbelConfig): ReqWarpper = url(s"${config.iamBaseUri}/$path")
  }
  object resources extends CorbelService {
    override def /(path: String)(implicit config: CorbelConfig): ReqWarpper = url(s"${config.resourceBaseUri}/$path")
  }
}

object CorbelHttpClient {
  implicit class ReqWarpper(val req: Req) extends AnyVal {
    def jsonContentType = req.setContentType("application/json", "UTF-8")
    def acceptJson = req.setHeader("Accept", "application/json")
  }
}