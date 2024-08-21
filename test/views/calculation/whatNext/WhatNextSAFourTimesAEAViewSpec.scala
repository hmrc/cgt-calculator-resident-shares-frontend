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

package views.calculation.whatNext

import assets.MessageLookup.WhatNextPages.{FourTimesAEA => pageMessages}
import assets.MessageLookup.{WhatNextPages => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.whatNext.whatNextSAFourTimesAEA

class WhatNextSAFourTimesAEAViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val whatNextSAFourTimesAEAView = fakeApplication.injector.instanceOf[whatNextSAFourTimesAEA]
  "The whatNextSAFourTimesAEA view" should {

    lazy val view = whatNextSAFourTimesAEAView("back-link")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${commonMessages.title}" in {
      doc.title() shouldBe commonMessages.title
    }

    "have a back link to 'back-link'" in {
      doc.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    "have the correct heading" in {
      doc.select("h1").text shouldBe commonMessages.heading
    }

    s"have the first paragraph of ${pageMessages.paragraphOne}" in {
      doc.select("#main-content > div > div > p:nth-child(2)").get(0).text shouldBe pageMessages.paragraphOne
    }

    s"have the second paragraph of ${pageMessages.paragraphTwo}" in {
      doc.select("#main-content > div > div > p:nth-child(3)").get(0).text shouldBe pageMessages.paragraphTwo
    }

    "have a finish button" which {

      lazy val finishButton = doc.select("#finish")

      s"has the text ${commonMessages.finish}" in {
        finishButton.text shouldBe commonMessages.finish
      }

      "has a link to the 'www.gov.uk' page" in {
        finishButton.attr("href") shouldBe "http://www.gov.uk"
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = whatNextSAFourTimesAEAView.f("back-link")(fakeRequest, mockMessage)

      val render = whatNextSAFourTimesAEAView.render("back-link", fakeRequest, mockMessage)

      f shouldBe render
    }
  }
}

