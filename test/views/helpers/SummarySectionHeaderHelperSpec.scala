/*
 * Copyright 2020 HM Revenue & Customs
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

package views.helpers

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers.resident.summarySectionHeaderHelper

class SummarySectionHeaderHelperSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)


  lazy val TestObject = summarySectionHeaderHelper("Heading")(mockMessage)
  lazy val h2 = Jsoup.parse(TestObject.body).select("H2")

  "The Summary Section Header Helper" should {

    s"have a H2 tag" which {

      "has the class 'heading-large'" in {
        h2.hasClass("heading-large") shouldBe true
      }

      "has the class 'summary-underline'" in {
        h2.hasClass("summary-underline") shouldBe true
      }

      "has the text 'Heading'" in {
        h2.text shouldBe "Heading"
      }
    }
  }
}
