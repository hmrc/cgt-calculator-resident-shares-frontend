/*
 * Copyright 2023 HM Revenue & Customs
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
import akka.stream.Materializer
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesProvider}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.warnings.sessionTimeout

class TimeoutControllerSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val config = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val mockMessagesProvider = mock[MessagesProvider]
  val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  implicit val mockApplication = fakeApplication
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit lazy val actorSystem = ActorSystem()
  lazy val materializer = mock[Materializer]
  val sessionTimeoutView = fakeApplication.injector.instanceOf[sessionTimeout]

  class fakeRequestTo(url: String, controllerAction: Action[AnyContent]) {
    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
    val result = controllerAction(fakeRequest)
    val jsoupDoc = Jsoup.parse(bodyOf(result)(materializer))

  }

  val controller = new TimeoutController(mockMCC, sessionTimeoutView)

  "TimeoutController.timeout" should {

    "when called with no session" should {

      object timeoutTestDataItem extends fakeRequestTo("", controller.timeout("test", "test2"))

      "return a 200" in {
        status(timeoutTestDataItem.result) shouldBe 200
      }

      s"have the home link too test2" in {
        timeoutTestDataItem.jsoupDoc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
      }

      "have the title" in {
        timeoutTestDataItem.jsoupDoc.getElementsByTag("title").text shouldEqual s"${Messages("session.timeout.message")} - Calculate your Capital Gains Tax - GOV.UK"
      }

      "contain the heading 'Your session has timeed out." in {
        timeoutTestDataItem.jsoupDoc.select("h1").text shouldEqual Messages("session.timeout.message")
      }

      "have a restart link to href of 'test'" in {
        timeoutTestDataItem.jsoupDoc.getElementById("startAgain").attr("href") shouldEqual "test"
      }
    }
  }
}
