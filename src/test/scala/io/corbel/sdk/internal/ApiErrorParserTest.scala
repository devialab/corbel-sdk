package io.corbel.sdk.internal

import java.io.ByteArrayInputStream

import com.ning.http.client.Response
import org.json4s.JsonAST.JObject
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

/**
  * @author Alexander De Leon (alex.deleon@devialab.com)
  */
class ApiErrorParserTest extends FlatSpec with Matchers with MockFactory {

 behavior of "ApiErrorParser"

  it should "parse all values from json error" in {
    val f = (res: Response) => "test"
    val parser = new ApiErrorParser(f)

    val jsonBody: JObject =
      ("error" -> "not_found") ~
      ("errorDescription" -> "Object not found")

    val responseMock = mock[Response]
    (responseMock.getStatusCode _).expects().anyNumberOfTimes().returning(404)
    (responseMock.hasResponseBody _).expects().returning(true)
    (responseMock.getResponseBodyAsStream _).expects().returning(new ByteArrayInputStream(compact(render(jsonBody)).getBytes()))

    val result = parser.apply(responseMock)
    result.isLeft should be(true)
    val apiError = result.left.get
    apiError.errorCode should be(Some("not_found"))
    apiError.message should be(Some("Object not found"))
    apiError.status should be(404)
  }

  it should "get description from body if there's no json response" in {
    val f = (res: Response) => "test"
    val parser = new ApiErrorParser(f)

    val body = "Boom!"
    val responseMock = mock[Response]
    (responseMock.getStatusCode _).expects().anyNumberOfTimes().returning(500)
    (responseMock.hasResponseBody _).expects().returning(true)
    (responseMock.getResponseBody _).expects().returning(body)
    (responseMock.getResponseBodyAsStream _).expects().returning(new ByteArrayInputStream(body.getBytes()))

    val result = parser.apply(responseMock)
    result.isLeft should be(true)
    val apiError = result.left.get
    apiError.message should be(Some(body))
    apiError.status should be(500)
  }

  it should "at least give me the error code if no body is sent" in {
    val f = (res: Response) => "test"
    val parser = new ApiErrorParser(f)
    val responseMock = mock[Response]
    (responseMock.getStatusCode _).expects().anyNumberOfTimes().returning(500)
    (responseMock.hasResponseBody _).expects().returning(false)

    val result = parser.apply(responseMock)
    result.isLeft should be(true)
    val apiError = result.left.get
    apiError.status should be(500)
  }
}
