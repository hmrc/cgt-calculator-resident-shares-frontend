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

package views.calculation.checkYourAnswers

import assets.MessageLookup.Resident.Shares.{ReviewAnswers => messages}
import assets.MessageLookup.{Resident => commonMessages}
import assets.ModelsAsset._
import assets.{MessageLookup => allMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Lang
import play.api.mvc.{Call, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import views.html.calculation.checkYourAnswers.checkYourAnswers

class CheckYourAnswersViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val fakeLang: Lang = Lang("en")
  val dummyBackLink = "backLink"
  val dummyPostCall: Call = Call("POST", "/dummy-url")
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val checkYourAnswersView = fakeApplication.injector.instanceOf[checkYourAnswers]

  lazy val view: HtmlFormat.Appendable = checkYourAnswersView(dummyPostCall, dummyBackLink, gainAnswersMostPossibles,
    Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers))(fakeRequestWithSession, mockMessage)
  lazy val doc: Document = Jsoup.parse(view.body)

  "have a charset of UTF-8" in {
    doc.charset().toString shouldBe "UTF-8"
  }

  s"have a title ${messages.title}" in {
    doc.title() shouldBe messages.title
  }

  s"have a back button" which {

    lazy val backLink = doc.select(".govuk-back-link")

    s"has the text '${commonMessages.back}'" in {
      backLink.text shouldBe commonMessages.back
    }

    s"has a link to '$dummyBackLink'" in {
      backLink.attr("href") shouldBe "#"
    }
  }

  s"have a page heading" which {

    s"includes a secondary heading with text '${messages.heading}'" in {
      doc.select("h1.govuk-heading-xl").text shouldBe messages.heading
    }
  }

  "have a section for the check your answers" in {
    doc.select("section").attr("id") shouldBe "yourAnswers"
  }

  "have a row for dates" which {

    "has the correct text" in {
      lazy val questionDiv = doc.select("#disposalDate-question")
      questionDiv.text shouldBe assets.MessageLookup.SharesDisposalDate.question
    }

    "has the correct value" in {
      lazy val amountDiv = doc.select("#disposalDate-date")
      amountDiv.text shouldBe "10 October 2016"
    }
  }

  "have a row for numeric values" which {
    "has the correct text" in {
      lazy val questionDiv = doc.select("#disposalValue-question")
      questionDiv.text shouldBe allMessages.Resident.Shares.SharesSummaryMessages.disposalValueQuestion
    }
    "has the correct value" in {
      lazy val amountDiv = doc.select("#disposalValue-amount")
      amountDiv.text shouldBe "£200,000"

    }
  }

  "have a row for option values" which {
    "has the correct text" in {
      lazy val questionDiv = doc.select("#soldForLessThanWorth-question")
      questionDiv.text shouldBe allMessages.Resident.Shares.SellForLess.title
    }
    "has the correct value" in {
      lazy val amountDiv = doc.select("#soldForLessThanWorth-option")
      amountDiv.text shouldBe "No"

    }
  }

  "have a continue button that" should {

    lazy val continueButton = doc.select("a.govuk-button")

    s"have the button text '${commonMessages.continue}'" in {
      continueButton.text shouldBe commonMessages.continue
    }

    "have an href of '/dummy-url'" in {
      continueButton.attr("href") shouldBe "/dummy-url"
    }

    "have the class 'button'" in {
      continueButton.hasClass("govuk-button") shouldBe true
    }
  }

  "CheckYourAnswers view" should {

    "generate the same template when .render and .f are called" in {

      val f = (checkYourAnswersView(dummyPostCall, dummyBackLink, gainAnswersMostPossibles,
        Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers), false)
        (fakeRequestWithSession,mockMessage))

      val render = checkYourAnswersView.render(dummyPostCall, dummyBackLink, gainAnswersMostPossibles,
        Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers), false,
      fakeRequestWithSession,mockMessage)

      f shouldBe render
    }
  }
}

