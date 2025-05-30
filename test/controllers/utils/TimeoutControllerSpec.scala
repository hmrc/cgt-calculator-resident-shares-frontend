/*
 * Copyright 2024 HM Revenue & Customs
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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.apache.pekko.actor.ActorSystem
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Messages, MessagesProvider}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import views.html.warnings.sessionTimeout

import scala.concurrent.Future

class TimeoutControllerSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val config: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val mockMessagesProvider: MessagesProvider = mock[MessagesProvider]
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  implicit val mockApplication: Application = fakeApplication
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit lazy val actorSystem: ActorSystem = ActorSystem()
  val sessionTimeoutView: sessionTimeout = fakeApplication.injector.instanceOf[sessionTimeout]

  class fakeRequestTo(url: String, controllerAction: Action[AnyContent]) {
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
    val result: Future[Result] = controllerAction(fakeRequest)
    val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

  }

  val controller = new TimeoutController(mockMCC, sessionTimeoutView)
  val homeLink: String = controllers.routes.GainController.disposalDate.url

  "TimeoutController.timeout" should {

    "when called with no session" should {

      object timeoutTestDataItem extends fakeRequestTo("", controller.timeout())

      "return a 200" in {
        status(timeoutTestDataItem.result) shouldBe 200
      }

      s"have the home link too test2" in {
        timeoutTestDataItem.jsoupDoc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldEqual homeLink
      }

      "have the title" in {
        timeoutTestDataItem.jsoupDoc.title() shouldEqual s"${Messages("session.timeout.message")} - Calculate your Capital Gains Tax - GOV.UK"
      }

      "contain the heading 'Your session has timed out." in {
        timeoutTestDataItem.jsoupDoc.select("h1").text shouldEqual Messages("session.timeout.message")
      }

      "have a restart link to href of 'test'" in {
        timeoutTestDataItem.jsoupDoc.getElementById("startAgain").attr("href") shouldEqual homeLink
      }
    }
  }
}
