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

package views.helpers

import assets.{MessageLookup => baseMessages}
import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.Resident.{Shares => sharesMessages}
import assets.MessageLookup.Resident.Shares.{SharesSummaryMessages => sharesSummaryMessages}
import assets.ModelsAsset._
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.playHelpers.checkYourAnswersPartial
import play.twirl.api.HtmlFormat
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents

class CheckYourAnswersPartialViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val fakeLang: Lang = Lang("en")
  val checkYourAnswersPartialView = fakeApplication.injector.instanceOf[checkYourAnswersPartial]

  "The check your answers partial with as much filled in as possible" should {

    lazy val view: HtmlFormat.Appendable = checkYourAnswersPartialView(gainAnswersMostPossibles,
      Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers))(mockMessage, fakeLang)
    lazy val doc: Document = Jsoup.parse(view.body)

    "has a date output row for the Disposal Date" which {

      s"should have the question text '${sharesSummaryMessages.disposalDateQuestion}'" in {
        doc.select("#disposalDate-question").text shouldBe sharesSummaryMessages.disposalDateQuestion
      }

      "should have the date '10 October 2016'" in {
        doc.select("#disposalDate-date").text shouldBe "10 October 2016"
      }

      s"should have a change link to ${routes.GainController.disposalDate.url}" in {
        doc.select("#disposalDate-change-link a").attr("href") shouldBe routes.GainController.disposalDate.url
      }

      "has the question as part of the link" in {
        doc.select("#disposalDate-change-link a").text shouldBe s"${commonMessages.change} ${sharesSummaryMessages.disposalDateQuestion}"
      }

      "has the question component of the link is govuk-visually-hidden" in {
        doc.select("#disposalDate-change-link a span.govuk-visually-hidden").text shouldBe sharesSummaryMessages.disposalDateQuestion
      }
    }

    "has a numeric output row for the Disposal Value" which {

      s"should have the question text '${sharesSummaryMessages.disposalValueQuestion}'" in {
        doc.select("#disposalValue-question").text shouldBe sharesSummaryMessages.disposalValueQuestion
      }

      "should have the value '£200,000'" in {
        doc.select("#disposalValue-amount").text shouldBe "£200,000"
      }

      s"should have a change link to ${routes.GainController.disposalValue.url}" in {
        doc.select("#disposalValue-change-link a").attr("href") shouldBe routes.GainController.disposalValue.url
      }

    }

    "has a numeric output row for the Disposal Costs" which {

      s"should have the question text '${sharesSummaryMessages.disposalCostsQuestion}'" in {
        doc.select("#disposalCosts-question").text shouldBe sharesSummaryMessages.disposalCostsQuestion
      }

      "should have the value '£10,000'" in {
        doc.select("#disposalCosts-amount").text shouldBe "£10,000"
      }

      s"should have a change link to ${routes.GainController.disposalCosts.url}" in {
        doc.select("#disposalCosts-change-link a").attr("href") shouldBe routes.GainController.disposalCosts.url
      }

    }

    "has an option/radiobutton output row for the Owned Before Start of Tax" which {

      s"should have the question text '${sharesMessages.OwnerBeforeLegislationStart.title}'" in {
        doc.select("#ownerBeforeLegislationStart-question").text shouldBe sharesMessages.OwnerBeforeLegislationStart.heading
      }

      "should have the value 'No'" in {
        doc.select("#ownerBeforeLegislationStart-option").text shouldBe "No"
      }

      s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart.url}" in {
        doc.select("#ownerBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart.url
      }
    }

    "has an option/radiobutton output row for Did You Inherit the Shares" which {

      s"should have the question text '${sharesMessages.DidYouInheritThem.question}'" in {
        doc.select("#inheritedTheShares-question").text shouldBe sharesMessages.DidYouInheritThem.question
      }

      "should have the value 'No'" in {
        doc.select("#inheritedTheShares-option").text shouldBe "No"
      }

      s"should have a change link to ${routes.GainController.didYouInheritThem.url}" in {
        doc.select("#inheritedTheShares-change-link a").attr("href") shouldBe routes.GainController.didYouInheritThem.url
      }
    }

    "does not have a numeric output row for the Inherited Value" in {
      doc.select("#worthWhenInherited-question").isEmpty shouldBe true
    }

    "has a numeric output row for the Acquisition Value" which {

      s"should have the question text '${sharesSummaryMessages.acquisitionValueQuestion}'" in {
        doc.select("#acquisitionValue-question").text shouldBe sharesSummaryMessages.acquisitionValueQuestion
      }

      "should have the value '£100,000'" in {
        doc.select("#acquisitionValue-amount").text shouldBe "£100,000"
      }

      s"should have a change link to ${routes.GainController.acquisitionValue.url}" in {
        doc.select("#acquisitionValue-change-link a").attr("href") shouldBe routes.GainController.acquisitionValue.url
      }

    }

    "has a numeric output row for the Acquisition Costs" which {

      s"should have the question text '${sharesSummaryMessages.acquisitionCostsQuestion}'" in {
        doc.select("#acquisitionCosts-question").text shouldBe sharesSummaryMessages.acquisitionCostsQuestion
      }

      "should have the value '£10,000'" in {
        doc.select("#acquisitionCosts-amount").text shouldBe "£10,000"
      }

      s"should have a change link to ${routes.GainController.acquisitionCosts.url}" in {
        doc.select("#acquisitionCosts-change-link a").attr("href") shouldBe routes.GainController.acquisitionCosts.url
      }
    }

    "has an option output row for brought forward losses" which {

      s"should have the question text '${baseMessages.LossesBroughtForward.question("2015 to 2016")}'" in {
        doc.select("#broughtForwardLosses-question").text shouldBe baseMessages.LossesBroughtForward.question("2015 to 2016")
      }

      "should have the value 'Yes'" in {
        doc.select("#broughtForwardLosses-option").text shouldBe "Yes"
      }

      s"should have a change link to ${routes.DeductionsController.lossesBroughtForward.url}" in {
        doc.select("#broughtForwardLosses-change-link a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward.url
      }

      "has the question as part of the link" in {
        doc.select("#broughtForwardLosses-change-link a").text shouldBe s"${commonMessages.change} ${baseMessages.LossesBroughtForward.question("2015 to 2016")}"
      }

      "has the question component of the link as govuk-visually-hidden" in {
        doc.select("#broughtForwardLosses-change-link a span.govuk-visually-hidden").text shouldBe baseMessages.LossesBroughtForward.question("2015 to 2016")
      }
    }

    "has a numeric output row for brought forward losses value" which {

      s"should have the question text '${baseMessages.LossesBroughtForwardValue.question("2015 to 2016")}'" in {
        doc.select("#broughtForwardLossesValue-question").text shouldBe baseMessages.LossesBroughtForwardValue.question("2015 to 2016")
      }

      "should have the value '£10,000'" in {
        doc.select("#broughtForwardLossesValue-amount").text shouldBe "£10,000"
      }

      s"should have a change link to ${routes.DeductionsController.lossesBroughtForwardValue.url}" in {
        doc.select("#broughtForwardLossesValue-change-link a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForwardValue.url
      }
    }

    "has a numeric output row for current income" which {

      s"should have the question text '${baseMessages.CurrentIncome.title("2015 to 2016")}'" in {
        doc.select("#currentIncome-question").text shouldBe baseMessages.CurrentIncome.question("2015 to 2016")
      }

      "should have the value '£0'" in {
        doc.select("#currentIncome-amount").text shouldBe "£0"
      }

      s"should have a change link to ${routes.IncomeController.currentIncome.url}" in {
        doc.select("#currentIncome-change-link a").attr("href") shouldBe routes.IncomeController.currentIncome.url
      }
    }

    "has a numeric output row for personal allowance" which {

      s"should have the question text '${baseMessages.PersonalAllowance.question("2015 to 2016")}'" in {
        doc.select("#personalAllowance-question").text shouldBe baseMessages.PersonalAllowance.question("2015 to 2016")
      }

      "should have the value '£0'" in {
        doc.select("#personalAllowance-amount").text shouldBe "£0"
      }

      s"should have a change link to ${routes.IncomeController.personalAllowance.url}" in {
        doc.select("#personalAllowance-change-link a").attr("href") shouldBe routes.IncomeController.personalAllowance.url
      }
    }
  }

  "The check your answers partial with display links set to false" should {
    lazy val view: HtmlFormat.Appendable = checkYourAnswersPartialView(gainAnswersMostPossibles,
      Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers), displayLinks = false)(mockMessage, fakeLang)
    lazy val doc: Document = Jsoup.parse(view.body)

    "have no links" in {
      doc.select("a").size() shouldBe 0
    }
  }

  "The check your answers partial with as little filled in as possible" should {

    lazy val view: HtmlFormat.Appendable = checkYourAnswersPartialView(gainAnswersLeastPossibles,
      Some(deductionAnswersLeastPossibles), Some(taxYearModel), None)(mockMessage, fakeLang)
    lazy val doc: Document = Jsoup.parse(view.body)

    "has an option output row for sold for less than worth value in" which {

      s"should have the question text '${sharesMessages.WorthWhenSoldForLess.question}'" in {
        doc.select("#worthWhenSoldForLess-question").text shouldBe sharesMessages.WorthWhenSoldForLess.question
      }

      "should have the value '£3,000'" in {
        doc.select("#worthWhenSoldForLess-amount").text shouldBe "£3,000"
      }

      s"should have a change link to ${routes.GainController.worthWhenSoldForLess.url}" in {
        doc.select("#worthWhenSoldForLess-change-link a").attr("href") shouldBe routes.GainController.worthWhenSoldForLess.url
      }

      "has the question as part of the link" in {
        doc.select("#worthWhenSoldForLess-change-link a").text shouldBe s"${commonMessages.change} " +
          s"${sharesMessages.WorthWhenSoldForLess.question}"
      }

      "has the question component of the link as govuk-visually-hidden" in {
        doc.select("#worthWhenSoldForLess-change-link a span.govuk-visually-hidden").text shouldBe
          sharesMessages.WorthWhenSoldForLess.question
      }
    }

    "has a numeric output row for the Disposal Costs" which {

      s"should have the question text '${sharesSummaryMessages.disposalCostsQuestion}'" in {
        doc.select("#disposalCosts-question").text shouldBe sharesSummaryMessages.disposalCostsQuestion
      }

      "should have the value '£200'" in {
        doc.select("#disposalCosts-amount").text shouldBe "£200"
      }

      s"should have a change link to ${routes.GainController.disposalCosts.url}" in {
        doc.select("#disposalCosts-change-link a").attr("href") shouldBe routes.GainController.disposalCosts.url
      }
    }

    "has an option/radiobutton output row for the Owned Before Start of Tax" which {

      s"should have the question text '${sharesMessages.OwnerBeforeLegislationStart.title}'" in {
        doc.select("#ownerBeforeLegislationStart-question").text shouldBe sharesMessages.OwnerBeforeLegislationStart.heading
      }

      "should have the value 'Yes'" in {
        doc.select("#ownerBeforeLegislationStart-option").text shouldBe "Yes"
      }

      s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart.url}" in {
        doc.select("#ownerBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart.url
      }
    }

    "has a numeric output row for the Worth on 31 March 1982 value" which {

      s"should have the question text '${sharesMessages.ValueBeforeLegislationStart.question}'" in {
        doc.select("#valueBeforeLegislationStart-question").text shouldBe sharesMessages.ValueBeforeLegislationStart.question
      }

      "should have the value '£5,000'" in {
        doc.select("#valueBeforeLegislationStart-amount").text shouldBe "£5,000"
      }

      s"should have a change link to ${routes.GainController.valueBeforeLegislationStart.url}" in {
        doc.select("#valueBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.valueBeforeLegislationStart.url
      }

    }

   "does not have a numeric output row for the current income" in {
      doc.select("#currentIncome-question").isEmpty shouldBe true
    }

    "does not have a numeric output row for the personal allowance" in {
      doc.select("#personalAllowance-question").isEmpty shouldBe true
    }

  }
}
