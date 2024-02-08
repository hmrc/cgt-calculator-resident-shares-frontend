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

import assets.MessageLookup.{WhatNextPages => commonMessages}
import assets.MessageLookup.WhatNextPages.{WhatNextGain => pageMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.whatNext.whatNextSAGain

class WhatNextSAGainViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val whatNextSAGainView = fakeApplication.injector.instanceOf[whatNextSAGain]

  "The whatNextSAGain view" should {

    lazy val view = whatNextSAGainView("back-link", "iFormUrl", "2016 to 2017")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${commonMessages.yourOptionsTitle}" in {
      doc.title() shouldBe commonMessages.yourOptionsTitle
    }

    "have a back link to 'back-link'" in {
      doc.select("a#back-link").attr("href") shouldBe "back-link"
    }

    "have the correct heading" in {
      doc.select("h1").text shouldBe commonMessages.yourOptionHEading
    }

    "have a bullet point list" which {

      s"has the title ${pageMessages.bulletPointTitle}" in {
        doc.select("#bullet-list-title").text shouldBe pageMessages.bulletPointTitle
      }

      s"has a first bullet point of ${pageMessages.bulletPointOne("2016 to 2017")}" in {
        doc.select("#main-content > div > div > div > ul > li:nth-child(1)").text shouldBe pageMessages.bulletPointOne("2016 to 2017")
      }

      s"has a second bullet point of ${pageMessages.bulletPointTwo}" in {
        doc.select("#main-content > div > div > div > ul > li:nth-child(2)").text shouldBe pageMessages.bulletPointTwo
      }
    }

    s"have an important information section with the text ${pageMessages.importantInformation}" in {
      doc.select("#important-information").text shouldBe pageMessages.importantInformation
    }

    "have a paragraph with the text ..." in {
      doc.select("#report-now-information").text shouldBe pageMessages.whatNextInformation
    }

    "have a Report now button" which {

      lazy val reportNowButton = doc.select("a#report-now-button")

      s"has the text ${commonMessages.reportNow}" in {
        reportNowButton.text shouldBe commonMessages.reportNow
      }

      "has the class button" in {
        reportNowButton.hasClass("govuk-button") shouldBe true
      }

      "has a link to the 'iFormUrl'" in {
        reportNowButton.attr("href") shouldBe "iFormUrl"
      }
    }

    "have exit survey text and link" which {

      "has the exit survey text" in {
        doc.select("#exit-survey-message").text shouldBe pageMessages.exitSurveyText
      }

      "has a link to the exit survey page" in {
        doc.select("#exit-survey-link").text() shouldBe pageMessages.exitSurveyLinkText
        doc.select("#exit-survey-link").attr("href") shouldBe  pageMessages.exitSurveyLink
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = whatNextSAGainView.f("back-link", "iFormUrl", "2016 to 2017")(fakeRequest, mockMessage)

      val render = whatNextSAGainView.render("back-link", "iFormUrl", "2016 to 2017", fakeRequest, mockMessage)

      f shouldBe render
    }
  }
}