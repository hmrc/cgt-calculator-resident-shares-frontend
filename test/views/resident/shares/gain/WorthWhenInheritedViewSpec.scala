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

package views.resident.shares.gain

import assets.MessageLookup.Resident.Shares.{WorthWhenInherited => Messages}
import assets.MessageLookup.Resident.{Shares => CommonSharesMessages}
import assets.MessageLookup.{Resident => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.WorthWhenInheritedForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.shares.{gain => views}

class WorthWhenInheritedViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "worthWhenInherited view" should {
    val form = worthWhenInheritedForm
    lazy val view = views.worthWhenInherited(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a back link to '${controllers.resident.shares.routes.GainController.didYouInheritThem().url}'" in {
      doc.select("#back-link").attr("href") shouldBe controllers.resident.shares.routes.GainController.didYouInheritThem().url
    }

    s"have a nav title of 'navTitle'" in {
      doc.select("span.header__menu__proposition-name").text() shouldBe commonMessages.homeText
    }

    s"have a home link to 'homeLink'" in {
      doc.select("a#homeNavHref").attr("href") shouldBe controllers.resident.shares.routes.GainController.disposalDate().url
    }

    s"have a title of ${Messages.question}" in {
      doc.title() shouldBe Messages.question
    }

    s"have a question of ${Messages.question}" in {
      doc.select("h1.heading-large").text() shouldBe Messages.question
    }

    "have a form tag" in {
      doc.select("form").size() shouldBe 1
    }

    s"have a form action of '${controllers.resident.shares.routes.GainController.submitWorthWhenInherited().url}'" in {
      doc.select("form").attr("action") shouldBe controllers.resident.shares.routes.GainController.submitWorthWhenInherited().url
    }

    "have a form method of 'POST'" in {
      doc.select("form").attr("method") shouldBe "POST"
    }

    s"have a label for an input with text ${Messages.question}" in {
      doc.select("label > span.visuallyhidden").text() shouldEqual Messages.question
    }

    s"have an input field with id amount " in {
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have a continue button " in {
      doc.select("#continue-button").text shouldBe commonMessages.continue
    }
  }

  "worthWhenInherited View with form without errors" should {
    val homeLink = "homeLink"
    val form = worthWhenInheritedForm.bind(Map("amount" -> "100"))
    lazy val view = views.worthWhenInherited(form)(fakeRequest)
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

  "worthWhenInherited View with form with errors" should {
    val form = worthWhenInheritedForm.bind(Map("amount" -> ""))
    lazy val view = views.worthWhenInherited(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
