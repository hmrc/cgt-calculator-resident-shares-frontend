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

import assets.MessageLookup.WhatNextNonSaLoss as messages
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import util.helper.ViewBehaviours
import views.html.calculation.whatNext.whatNextNonSaLoss

class WhatNextNonSaLossViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with ViewBehaviours {
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val whatNextNonSaLossView: whatNextNonSaLoss = fakeApplication.injector.instanceOf[whatNextNonSaLoss]

  "whatNextNonSaLoss view" should {

    lazy val view = whatNextNonSaLossView("iFormUrl")(using fakeRequestWithSession, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"return a title of ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select(".govuk-back-link")

      "have the correct back link text" in {
        backLink.text shouldBe messages.back
      }

      "have the back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "have a link to Confirm self assessment" in {
        backLink.attr("href") shouldBe "#"
      }
    }

    s"heading of ${messages.heading}" should {
      behave like pageWithExpectedMessage(headingStyle, messages.heading)(using doc)
    }

    s"have a first paragraph with text ${messages.detailsOne}" in {
      doc.select("#details-one").text shouldBe messages.detailsOne
    }

    s"have a second paragraph with text ${messages.detailsTwo}" in {
      doc.select("#details-two").text shouldBe messages.detailsTwo
    }

    "have a report now link" which {

      lazy val reportNow = doc.select("#report-now").select("a")

      s"has text ${messages.reportNow}" in {
        reportNow.text shouldBe messages.reportNow
      }

      "has a link to 'iFormUrl'" in {
        reportNow.attr("href") shouldBe "iFormUrl"
      }
    }

    "have a return to GOV.UK link" which {

      lazy val govUk = doc.select("#govUk").select("a")

      s"has text ${messages.govUk}" in {
        govUk.text shouldBe messages.govUk
      }

      "has a link to 'http://www.gov.uk'" in {
        govUk.attr("href") shouldBe "http://www.gov.uk"
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = whatNextNonSaLossView.f("iFormUrl")(fakeRequestWithSession, mockMessage)

      val render = whatNextNonSaLossView.render("iFormUrl", fakeRequestWithSession, mockMessage)

      f shouldBe render
    }
  }
}
