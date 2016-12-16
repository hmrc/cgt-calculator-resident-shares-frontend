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

import controllers.helpers.FakeRequestHelper
import forms.DidYouInheritThemForm._
import models.resident.shares.gain.DidYouInheritThemModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{gain => views}
import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.Resident.Shares.{DidYouInheritThem => messages}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class DidYouInheritThemViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Sell for less view with an empty form" should {

    lazy val view = views.didYouInheritThem(didYouInheritThemForm)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)
    lazy val form = doc.getElementsByTag("form")

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.question}" in {
      doc.title shouldBe messages.question
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${messages.question}'" in {
        h1Tag.text shouldBe messages.question
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }

    s"have the home link to 'home'" in {
      doc.select("#homeNavHref").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        backLink.attr("href") shouldBe "/calculate-your-capital-gains/resident/shares/owner-before-legislation-start"
      }
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/shares/did-you-inherit-them"
    }

    "has the method of POST" in {
      form.attr("method") shouldBe "POST"
    }

    "have a legend for the radio inputs" which {

      lazy val legend = doc.select("legend")

      s"contain the text ${messages.question}" in {
        legend.text should include(s"${messages.question}")
      }

      "that is visually hidden" in {
        legend.hasClass("visuallyhidden") shouldEqual true
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a div with class form-group" in {
        doc.select("div#radio-input").hasClass("form-group") shouldEqual true
      }

      "for the option 'Yes'" should {

        lazy val YesRadioOption = doc.select(".block-label[for=wereInherited-yes]")

        "have a label with class 'block-label'" in {
          YesRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          YesRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value wereInherited-Yes" in {
          YesRadioOption.attr("for") shouldEqual "wereInherited-yes"
        }

        "have the text 'Yes'" in {
          YesRadioOption.text shouldEqual "Yes"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#wereInherited-yes")

          "have the id 'wereInherited-Yes'" in {
            optionLabel.attr("id") shouldEqual "wereInherited-yes"
          }

          "have the value 'Yes'" in {
            optionLabel.attr("value") shouldEqual "Yes"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'No'" should {

        lazy val NoRadioOption = doc.select(".block-label[for=wereInherited-no]")

        "have a label with class 'block-label'" in {
          NoRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          NoRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value wereInherited-No" in {
          NoRadioOption.attr("for") shouldEqual "wereInherited-no"
        }

        "have the text 'No'" in {
          NoRadioOption.text shouldEqual "No"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#wereInherited-no")

          "have the id 'livedInProperty-No'" in {
            optionLabel.attr("id") shouldEqual "wereInherited-no"
          }

          "have the value 'No'" in {
            optionLabel.attr("value") shouldEqual "No"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
    }

    "have a continue button" which {

      lazy val button = doc.select("button")

      "has class 'button'" in {
        button.hasClass("button") shouldEqual true
      }

      "has attribute 'type'" in {
        button.hasAttr("type") shouldEqual true
      }

      "has type value of 'submit'" in {
        button.attr("type") shouldEqual "submit"
      }

      "has attribute id" in {
        button.hasAttr("id") shouldEqual true
      }

      "has id equal to continue-button" in {
        button.attr("id") shouldEqual "continue-button"
      }

      s"has the text ${commonMessages.continue}" in {
        button.text shouldEqual s"${commonMessages.continue}"
      }
    }
  }

  "Sell for less view with a filled form" which {

    "for the option 'Yes'" should {
      lazy val view = views.didYouInheritThem(didYouInheritThemForm.fill(DidYouInheritThemModel(true)))(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)
      lazy val YesRadioOption = doc.select(".block-label[for=wereInherited-yes]")

      "have the option auto-selected" in {
        YesRadioOption.attr("class") shouldBe "block-label selected"
      }
    }

    "for the option 'No'" should {
      lazy val view = views.didYouInheritThem(didYouInheritThemForm.fill(DidYouInheritThemModel(false)))(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)
      lazy val NoRadioOption = doc.select(".block-label[for=wereInherited-no]")

      "have the option auto-selected" in {
        NoRadioOption.attr("class") shouldBe "block-label selected"
      }
    }
  }

  "Sell for less view with form errors" should {

    lazy val form = didYouInheritThemForm.bind(Map("wereInherited" -> ""))
    lazy val view = views.didYouInheritThem(form)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have an error summary" which {
      "display an error summary message for the page" in {
        doc.body.select("#wereInherited-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".form-group .error-notification").size shouldBe 1
      }
    }
  }

}
