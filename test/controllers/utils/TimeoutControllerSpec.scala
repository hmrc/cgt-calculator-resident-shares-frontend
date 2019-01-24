/*
 * Copyright 2019 HM Revenue & Customs
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
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class TimeoutControllerSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem = ActorSystem()
  lazy val materializer = mock[Materializer]
  implicit val config = fakeApplication.injector.instanceOf[ApplicationConfig]

  class fakeRequestTo(url: String, controllerAction: Action[AnyContent]) {
    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
    val result = controllerAction(fakeRequest)
    val jsoupDoc = Jsoup.parse(bodyOf(result)(materializer))

  }

  val controller = new TimeoutController()

  "TimeoutController.timeout" should {

    "when called with no session" should {

      object timeoutTestDataItem extends fakeRequestTo("", controller.timeout("test", "test2"))

      "return a 200" in {
        status(timeoutTestDataItem.result) shouldBe 200
      }

      s"have the home link too test2" in {
        timeoutTestDataItem.jsoupDoc.select("#homeNavHref").attr("href") shouldEqual "test2"
      }

      "have the title" in {
        timeoutTestDataItem.jsoupDoc.getElementsByTag("title").text shouldEqual Messages("session.timeout.message")
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
