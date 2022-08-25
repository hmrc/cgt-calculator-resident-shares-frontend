/*
 * Copyright 2022 HM Revenue & Customs
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

import assets.MessageLookup.Resident.Shares.{WorthWhenInherited => Messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.WorthWhenInheritedForm._
import org.jsoup.Jsoup
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.gain.worthWhenInherited

class WorthWhenInheritedViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val worthWhenInheritedView = fakeApplication.injector.instanceOf[worthWhenInherited]

  "worthWhenInherited view" should {
    lazy val form = worthWhenInheritedForm
    lazy val view = worthWhenInheritedView(form)(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a back link to '${controllers.routes.GainController.didYouInheritThem().url}'" in {
      doc.select("#back-link").attr("href") shouldBe controllers.routes.GainController.didYouInheritThem().url
    }

    s"have a nav title of 'navTitle'" in {
      doc.select("body > header > div > div > div.govuk-header__content > a").text() shouldBe commonMessages.homeText
    }

    s"have a home link to 'homeLink'" in {
      doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldBe controllers.routes.GainController.disposalDate().url
    }

    s"have a title of ${Messages.title}" in {
      doc.title() shouldBe Messages.title
    }

    s"have a question of ${Messages.question}" in {
      doc.select("h1.govuk-heading-xl").text() shouldBe Messages.question
    }

    "have a form tag" in {
      doc.select("form").size() shouldBe 1
    }

    s"have a form action of '${controllers.routes.GainController.submitWorthWhenInherited().url}'" in {
      doc.select("form").attr("action") shouldBe controllers.routes.GainController.submitWorthWhenInherited().url
    }

    "have a form method of 'POST'" in {
      doc.select("form").attr("method") shouldBe "POST"
    }

    s"have a label for an input with text ${Messages.question}" in {
      doc.select("label").text() shouldEqual Messages.question
    }

    s"have an input field with id amount " in {
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "has help text that" should {

      s"have the text ${Messages.helpText}" in {
        doc.body.getElementsByClass("govuk-hint").text shouldBe Messages.helpText
      }
    }

    "have a p tag" which {
      lazy val form = doc.getElementsByTag("form")
      s"with the extra text ${Messages.hintText}" in {
        form.select("p.govuk-inset-text").text shouldBe Messages.hintText
      }
    }

    "have a continue button " in {
      doc.body.getElementsByClass("govuk-button").text shouldBe commonMessages.continue
    }

    "generate the same template when .render and .f are called" in {

      val f = worthWhenInheritedView.f(form)(fakeRequest, mockMessage)

      val render = worthWhenInheritedView.render(form, fakeRequest, mockMessage)

      f shouldBe render
    }
  }


  "worthWhenInherited View with form without errors" should {
    lazy val form = worthWhenInheritedForm.bind(Map("amount" -> "100"))
    lazy val view = worthWhenInheritedView(form)(fakeRequest, mockMessage)
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

  "worthWhenInherited View with form with errors" should {
    lazy val form = worthWhenInheritedForm.bind(Map("amount" -> ""))
    lazy val view = worthWhenInheritedView(form)(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 1
    }
  }
}
