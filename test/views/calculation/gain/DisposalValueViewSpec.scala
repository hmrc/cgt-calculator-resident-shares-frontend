/*
 * Copyright 2016 HM Revenue & Customs
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
import controllers.helpers.FakeRequestHelper
import forms.DisposalValueForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{gain => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class DisposalValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  case class FakePOST(value: String) {
    lazy val request = fakeRequestToPOSTWithSession(("amount", value))
    lazy val form = disposalValueForm.bind(Map(("amount", value)))
    lazy val view = views.disposalValue(form, "home-link")(request, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)
  }

  "Disposal Value View" should {

    lazy val view = views.disposalValue(disposalValueForm, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    "have a home link to 'home-link'" in {
      doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
    }

    s"have the title of the page ${messages.question}" in {
      doc.title shouldEqual messages.question
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
      doc.select("label > span.visuallyhidden").text() shouldEqual messages.question
    }

    s"have an input field with id amount " in {
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have continue button " in {
      doc.body.getElementById("continue-button").text shouldEqual commonMessages.continue
    }
  }

  "Disposal Value View with form without errors" should {

    lazy val form = disposalValueForm.bind(Map("amount" -> "100"))
    lazy val view = views.disposalValue(form, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "Disposal Value View with form with errors" should {

    lazy val form = disposalValueForm.bind(Map("amount" -> ""))
    lazy val view = views.disposalValue(form, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
