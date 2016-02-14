package io.corbel.sdk.internal

import com.ning.http.client.Response
import grizzled.slf4j.Logging
import io.corbel.sdk.ApiError
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JValue, JString, JObject}
import org.json4s.native.JsonMethods._

import scala.util.{Success, Try}

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class ApiErrorParser[T](f: Response => T) extends (Response => Either[ApiError, T]) with Logging {
  override def apply(res: Response): Either[ApiError, T] = res.getStatusCode match {
    case ok: Int if ok / 100 == 2 => Right(f(res))
    case other: Int => Left(apiErrorBody(res))
  }

  def apiErrorBody(res: Response): ApiError = {
    val apiError = ApiError(status = res.getStatusCode)
    if(!res.hasResponseBody){
      apiError
    }
    else {
      Try(parse(res.getResponseBodyAsStream)) match {
        case Success(json) => completeFromJson(apiError, json)
        case _ =>
          warn(s"Corbel error message without expected JSON body: ${res.getStatusCode} : ${res.getResponseBody}")
          apiError.copy(message = Option(res.getResponseBody))
      }
    }
  }

  def completeFromJson(apiError: ApiError, json: JValue): ApiError = {
    implicit val format = DefaultFormats
    val c = (json \ "error").extractOpt[String]
    val m = (json \ "errorDescription").extractOpt[String]
    apiError.copy(errorCode = c, message = m)
  }

}
