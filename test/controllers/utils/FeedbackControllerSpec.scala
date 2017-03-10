/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.utils

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.AppConfig
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.mvc.{AnyContent, Request, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.partials.{CachedStaticHtmlPartialRetriever, FormPartialRetriever, HtmlPartial}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class FeedbackControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  implicit lazy val actorSystem = ActorSystem()
  implicit lazy val mat = ActorMaterializer()

  val fakeRequest = FakeRequest("GET", "/")

  val mockHttp = mock[WSHttp]

  val target = new FeedbackController {
    override implicit val cachedStaticHtmlPartialRetriever: CachedStaticHtmlPartialRetriever = new CachedStaticHtmlPartialRetriever {
      override def httpGet: HttpGet = ???

      override def getPartialContent(url: String, templateParameters: Map[String, String], errorMessage: Html)(implicit request: RequestHeader): Html =
        Html("")
    }
    override implicit val formPartialRetriever: FormPartialRetriever = new FormPartialRetriever {
      override def crypto: (String) => String = ???

      override def httpGet: HttpGet = ???

      override def getPartialContent(url: String, templateParameters: Map[String, String], errorMessage: Html)(implicit request: RequestHeader): Html = Html("")
    }

    protected def authConnector: AuthConnector = ???

    protected def loadPartial(url: String)(implicit request: RequestHeader): HtmlPartial = ???

    override def httpPost: HttpPost = mockHttp

    override def localSubmitUrl(implicit request: Request[AnyContent]): String = ""

    override def contactFormReferer(implicit request: Request[AnyContent]): String = request.headers.get(REFERER).getOrElse("")

    override val applicationConfig: AppConfig = new AppConfig {
      override val assetsPrefix: String = ""
      override val reportAProblemNonJSUrl: String = ""
      override val contactFrontendPartialBaseUrl: String = ""
      override val analyticsHost: String = ""
      override val analyticsToken: String = ""
      override val reportAProblemPartialUrl: String = ""
      override val contactFormServiceIdentifier: String = ""
    }
  }

  "GET /feedback" should {
    "return feedback page" in {
      val result = target.show(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "capture the referer in the session on initial session on the feedback load" in {
      val result = target.show(fakeRequest.withHeaders("Referer" -> "Blah"))
      status(result) shouldBe Status.OK
    }
  }

  "POST /feedback" should {
    val fakePostRequest = FakeRequest("POST", "/calculate-your-capital-gains/feedback").withFormUrlEncodedBody("test" -> "test")
    "return form with thank you for valid selections" in {
      when(mockHttp.POSTForm[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
        Future.successful(HttpResponse(Status.OK, responseString = Some("1234"))))

      val result = target.submit(fakePostRequest)
      redirectLocation(result) shouldBe Some(routes.FeedbackController.thankyou().url)
    }

    "return form with errors for invalid selections" in {
      when(mockHttp.POSTForm[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
        Future.successful(HttpResponse(Status.BAD_REQUEST, responseString = Some("<p>:^(</p>"))))
      val result = target.submit(fakePostRequest)
      status(result) shouldBe Status.BAD_REQUEST
    }

    "return error for other http code back from contact-frontend" in {
      when(mockHttp.POSTForm[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
        Future.successful(HttpResponse(418))) // 418 - I'm a teapot
      val result = target.submit(fakePostRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return internal server error when there is an empty form" in {
      when(mockHttp.POSTForm[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
        Future.successful(HttpResponse(Status.OK, responseString = Some("1234"))))

      val result = target.submit(fakeRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "GET /feedback/thankyou" should {
    "should return the thank you page" in {
      val result = target.thankyou(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }
}
