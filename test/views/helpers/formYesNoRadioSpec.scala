/*
 * Copyright 2017 HM Revenue & Customs
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

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.OtherPropertiesForm._
import org.jsoup.Jsoup
import views.html.helpers._
import assets.MessageLookup.{NonResident => messages}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class formYesNoRadioSpec extends UnitSpec with WithFakeApplication {

  "formYesNoRadio" when {

    "not supplied with help text or a legend class" should {
      lazy val helper = formYesNoRadio(otherPropertiesForm.apply("hasOtherProperties"), "legend")(applicationMessages)
      lazy val document = Jsoup.parse(helper.body)

      "contain inputs with the id hasOtherProperties" in {
        document.select("input").attr("id") should include ("hasOtherProperties")
      }

      "contain an input with the value 'Yes'" in {
        document.select("[for=hasOtherProperties-yes] input").attr("value") shouldBe "Yes"
      }

      "contain an input with the value 'No'" in {
        document.select("[for=hasOtherProperties-no] input").attr("value") shouldBe "No"
      }

      s"contain an label with the message ${messages.yes}" in {
        document.select("[for=hasOtherProperties-yes]").text() shouldBe messages.yes
      }

      s"contain an label with the message ${messages.no}" in {
        document.select("[for=hasOtherProperties-no]").text() shouldBe messages.no
      }

      "have a legend" which {
        lazy val legend = document.select("legend")

        "have a legend with no class set" in {
          legend.attr("class").isEmpty shouldBe true
        }

        "have a legend with the text 'legend'" in {
          legend.text() shouldBe "legend"
        }

        "have a legend with the id 'hasOtherProperties'" in {
          legend.attr("id") shouldBe "hasOtherProperties"
        }
      }

      "not have any help text" in {
        document.select("span.form-hint").size() shouldBe 0
      }

      "have labels with the class block-label" in {
        document.select("label").attr("class") shouldBe "block-label"
      }

      "have a fieldset with the class 'inline form-group radio-list'" in {
        document.select("fieldset").attr("class") shouldBe "inline form-group radio-list"
      }
    }

    "supplied with help text but no legend" should {
      lazy val helper = formYesNoRadio(otherPropertiesForm.apply("hasOtherProperties"), "legend", helpText = Some("help"))(applicationMessages)
      lazy val document = Jsoup.parse(helper.body)

      "have some help text of help" in {
        document.select("span.form-hint").text() shouldBe "help"
      }

      "have a legend with no class set" in {
        document.select("legend").attr("class").isEmpty shouldBe true
      }
    }

    "supplied with no help text but with a legend" should {
      lazy val helper = formYesNoRadio(otherPropertiesForm.apply("hasOtherProperties"), "legend", legendClass = Some("class"))(applicationMessages)
      lazy val document = Jsoup.parse(helper.body)

      "not have any help text" in {
        document.select("span.form-hint").size() shouldBe 0
      }

      "have a legend with a class of class" in {
        document.select("legend").attr("class") shouldBe "class"
      }
    }

    "supplied with both help text and a legend" should {
      lazy val helper = formYesNoRadio(otherPropertiesForm.apply("hasOtherProperties"), "legend", helpText = Some("help"),
        legendClass = Some("class"))(applicationMessages)
      lazy val document = Jsoup.parse(helper.body)

      "have some help text of help" in {
        document.select("span.form-hint").text() shouldBe "help"
      }

      "have a legend with a class of class" in {
        document.select("legend").attr("class") shouldBe "class"
      }
    }
  }
}
