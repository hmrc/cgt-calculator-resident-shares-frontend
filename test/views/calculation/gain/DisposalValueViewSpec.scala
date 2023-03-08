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

package views.calculation.gain

import assets.MessageLookup.Resident.Shares.{DisposalValue => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.DisposalValueForm._
import org.jsoup.Jsoup
import views.html.calculation.gain.disposalValue
import play.api.mvc.MessagesControllerComponents

class DisposalValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val disposalValueView = fakeApplication.injector.instanceOf[disposalValue]
  
  case class FakePOST(value: String) {
    lazy val request = fakeRequestToPOSTWithSession(("amount", value)).withMethod("POST")
    lazy val form = disposalValueForm.bind(Map(("amount", value)))
    lazy val view = disposalValueView(form, "home-link")(request, mockMessage)
    lazy val doc = Jsoup.parse(view.body)
  }

  "Disposal Value View" should {

    lazy val view = disposalValueView(disposalValueForm, "home-link")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    "have a home link to 'home-link'" in {
      doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }

    s"have the title of the page ${messages.title}" in {
      doc.title shouldEqual messages.title
    }

    s"have a back link to the Sell For Less Page with text ${commonMessages.back}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/sell-for-less"
    }

    s"have the question of the page ${messages.question}" in {
      doc.select("h1").text shouldEqual messages.question
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-value"
    }

    s"have a label for an input with text ${messages.question}" in {
      doc.body.getElementsByClass("govuk-label govuk-visually-hidden").text() shouldEqual messages.question
    }

    s"have an input field with id amount " in {
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have continue button " in {
      doc.body.getElementById("submit").text shouldEqual commonMessages.continue
    }

    "have the joint ownership text" in {
      doc.select("p.govuk-inset-text").text shouldBe messages.jointOwnership
    }

    "generate the same template when .render and .f are called" in {

      val f = disposalValueView.f(disposalValueForm, "home-link")(fakeRequest, mockMessage)

      val render = disposalValueView.render(disposalValueForm, "home-link", fakeRequest, mockMessage)

      f shouldBe render
    }

  }

  "Disposal Value View with form without errors" should {

    lazy val form = disposalValueForm.bind(Map("amount" -> "100"))
    lazy val view = disposalValueView(form, "home-link")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 0
    }
  }

  "Disposal Value View with form with errors" should {

    lazy val form = disposalValueForm.bind(Map("amount" -> ""))
    lazy val view = disposalValueView(form, "home-link")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 1
    }
  }
}
