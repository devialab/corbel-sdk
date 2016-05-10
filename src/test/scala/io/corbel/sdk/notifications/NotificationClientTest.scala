package io.corbel.sdk.notifications

import io.corbel.sdk.auth.AuthenticationProvider
import io.corbel.sdk.config.CorbelConfig
import org.json4s._
import org.json4s.Diff._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.scalap.scalasig.ClassFileParser
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer._
import org.mockserver.model.HttpRequest._
import org.mockserver.model.HttpResponse._
import org.mockserver.model.JsonSchemaBody._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by ismael on 10/05/16.
  */
class NotificationClientTest extends FlatSpec with Matchers with BeforeAndAfter with ScalaFutures with PatienceConfiguration {

  implicit val formats = DefaultFormats
  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  var mockServer: ClientAndServer = null
  implicit val config = CorbelConfig(
    iamBaseUri = "http://localhost:1080",
    resourceBaseUri = "http://localhost:1080",
    notificationsBaseUri = "http://localhost:1080",
    defaultTokenExpiration = 300000 millis
  )
  val clientId = "123"
  val cleintSecret = "567"
  val testToken = "AAAABBBCCCC"

  behavior of "send notification"

  val expectedJson: String =
    """
      |{
      | "notificationId":"notification",
      |  "recipient":"email",
      |  "properties":{
      |   "property1": "value1",
      |   "property2": "value2"
      | }
      |}
    """.stripMargin
  it should "make request to PUT notifications send endpoint" in {
    implicit val auth = AuthenticationProvider(testToken)
    mockServer
      .when(sendNotification)
      .respond(
        response()
          .withStatusCode(204)
          .withHeader("Content-Type", "application/json")
      )

    val notifications = NotificationsClient()
    val futureResponse = notifications.sendNotification("notification", "email", new mutable.HashMap[String, String]() + ("property1" -> "value1") + ("property2" -> "value2"))

    whenReady(futureResponse) { response => {
      response should be(Right())
      val recorded = mockServer.retrieveRecordedRequests(sendNotification)
      diff(parse(recorded(0).getBodyAsString), parse(expectedJson)) should be (Diff(JNothing, JNothing, JNothing))
    }
    }
  }


  def sendNotification = request()
    .withMethod("POST")
    .withPath(s"/v1.0/notification/send")
    .withHeader("Authorization", s"Bearer $testToken")


  before {
    mockServer = startClientAndServer(1080)
  }

  after {
    mockServer.stop()
  }

}
