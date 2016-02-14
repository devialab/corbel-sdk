package io.corbel.sdk.error

import com.ning.http.client.Response

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
case class ApiError(status: Int, errorCode: Option[String] = None, message: Option[String] = None)

object ApiError {
  implicit class ResponseHanderWrapper[T](val f: Response => T) extends AnyVal {
    def eitherApiError = new ApiErrorParser[T](f)
  }
}