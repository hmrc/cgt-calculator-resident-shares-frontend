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

package views.helpers

import assets.MessageLookup.{SummaryDetails => summaryMessages}
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares.GainAnswersModel
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents
import views.html.playHelpers.gainSummaryPartial

class GainSummaryPartialViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val gainSummaryPartialView: gainSummaryPartial = fakeApplication.injector.instanceOf[gainSummaryPartial]
  val fakeLang: Lang = Lang("en")
  
  "The shares were sold for less than worth" should {

    val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      soldForLessThanWorth = true,
      disposalValue = Some(100),
      worthWhenSoldForLess = Some(10),
      disposalCosts = BigDecimal(100000),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(10000),
      acquisitionCosts = BigDecimal(100000)
    )

    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val view = gainSummaryPartialView(gainAnswers, taxYearModel, -100, 150, 11000)(using fakeRequestWithSession, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "has a banner" which {
      lazy val banner = doc.select("#tax-owed-banner")

      "contains a h1" which {
        lazy val h1 = banner.select("h1")

        s"has the text ${summaryMessages.cgtToPay("2015 to 2016")}" in {
          h1.text() shouldEqual summaryMessages.cgtToPay("2015 to 2016")
        }
      }
      "contains a h2" which {
        lazy val h2 = banner.select("h2")

        s"has the text '£0.00'" in {
          h2.text() shouldEqual "£0.00"
        }
      }
    }

    "does not have a notice summary" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe true
    }

    "have a section for the Calculation details" which {

      "has a h2 tag" which {

        s"has the text '${summaryMessages.howWeWorkedThisOut}'" in {
          doc.select("#calcHeader").text shouldBe summaryMessages.howWeWorkedThisOut
        }
      }

      "has a div for total loss" which {

        lazy val div = doc.select("#yourTotalLoss")

        "has a caption tag" which {

          s"has the text '${summaryMessages.yourTotalLoss}'" in {
            div.select("div > h2").text shouldBe summaryMessages.yourTotalLoss
          }
        }

        "has a row for disposal value" which {
          s"has the text '${summaryMessages.disposalValue}'" in {
            doc.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
          }

          "has the value '£10'" in {
            doc.select("#disposalValue-amount span").text shouldBe "£10"
          }
        }

        "has a row for acquisition value" which {
          s"has the text '${summaryMessages.acquisitionValue}'" in {
            doc.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
          }

          "has the value '£10,000'" in {
            doc.select("#acquisitionValue-amount span").text shouldBe "£10,000"
          }
        }

        "has a row for total costs" which {
          s"has the text '${summaryMessages.totalCosts}'" in {
            doc.select("#totalCosts-text").text shouldBe summaryMessages.totalCosts
          }

          "has the value '£150'" in {
            doc.select("#totalCosts-amount span").text shouldBe "£150"
          }
        }

        "has a row for total loss" which {
          s"has the text '${summaryMessages.totalLoss}'" in {
            doc.select("#totalLoss-text").text shouldBe summaryMessages.totalLoss
          }

          "has the value '£100'" in {
            doc.select("#totalLoss-amount strong").text shouldBe "£100"
          }
        }
      }

      "has a div for deductions" which {

        lazy val div = doc.select("#yourDeductions")

        "has a caption tag" which {

          s"has the text '${summaryMessages.yourDeductions}'" in {
            div.select("div > h2").text shouldBe summaryMessages.yourDeductions
          }
        }

        "has a row for AEA used" which {

          s"has the text '${summaryMessages.aeaUsed}'" in {
            doc.select("#aeaUsed-text").text shouldBe summaryMessages.aeaUsed
          }

          "has the value '£0'" in {
            div.select("#aeaUsed-amount").text shouldBe "£0"
          }
        }

        "not have a row for brought forward losses used" in {
          doc.select("#lossesUsed-text") shouldBe empty
        }

        "has a row for total deductions" which {

          s"has the text '${summaryMessages.totalDeductions}'" in {
            doc.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
          }

          "has the value '£0'" in {
            doc.select("#totalDeductions-amount strong").text shouldBe "£0"
          }
        }
      }

      "has a div for Taxable Gain" which {

        lazy val div = doc.select("#yourTaxableGain")

        "does not have a h2 tag" in {
          div.select("h2") shouldBe empty
        }

        "does not have a row for gain" in {
          doc.select("#gain-text") shouldBe empty
        }

        "does not have a row for minus deductions" in {
          doc.select("#minusDeductions-text") shouldBe empty
        }

        "has a row for taxable gain" which {
          s"has the text '${summaryMessages.taxableGain}'" in {
            doc.select("#taxableGain-text").text shouldBe summaryMessages.taxableGain
          }

          "has the value '£0'" in {
            doc.select("#taxableGain-amount strong").text shouldBe "£0"
          }
        }
      }

      "has a div for tax rate" which {

        lazy val div = doc.select("#yourTaxRate")

        "does not have a h2 tag" in {
          div.select("h2") shouldBe empty
        }

        "does not have a row for first band"  in {
          doc.select("#firstBand-text") shouldBe empty
        }

        "does not have a row for second band" in {
          doc.select("#secondBand-text") shouldBe empty
        }

        "has a row for tax to pay" which {

          s"has the text ${summaryMessages.taxToPay}" in {
            doc.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
          }

          "has the value '£0'" in {
            doc.select("#taxToPay-amount").text shouldBe "£0"
          }
        }
      }
    }

    "have a section for the Your remaining deductions" which {

      "has a div for remaining deductions" which {

        lazy val div = doc.select("#remainingDeductions")

        "has a caption tag" which {

          s"has the text ${summaryMessages.remainingDeductions}" in {
            div.select("div > h2").text shouldBe summaryMessages.remainingDeductions
          }
        }

        "has a row for annual exempt amount left" which {
          s"has the text ${summaryMessages.remainingAnnualExemptAmount("2015 to 2016")}" in {
            doc.select("#aeaLeft-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2015 to 2016")
          }

          "has the value '£11,000'" in {
            doc.select("#aeaLeft-amount span").text shouldBe "£11,000"
          }
        }

        "not have a row for brought forward losses remaining" in {
          doc.select("#broughtForwardLossesRemaining-text") shouldBe empty
        }

        "has a row for losses to carry forward" which {
          s"has the text${summaryMessages.lossesToCarryForwardFromCalculation}" in {
            doc.select("#lossesToCarryForwardFromCalc-text").text shouldBe summaryMessages.lossesToCarryForwardFromCalculation
          }

          "has the value '£100" in {
            doc.select("#lossesToCarryForwardFromCalc-amount span").text shouldBe "£100"
          }
        }
      }
    }
  }

  "The shares sale price is equal to the losses" should {

    val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      soldForLessThanWorth = true,
      disposalValue = Some(100),
      worthWhenSoldForLess = Some(10),
      disposalCosts = BigDecimal(100000),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(10000),
      acquisitionCosts = BigDecimal(100000)
    )

    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val view = gainSummaryPartialView(gainAnswers, taxYearModel, 0, 150, 11000)(using fakeRequestWithSession, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "has a caption tag" which {

      lazy val div = doc.select("#yourTotalLoss")


      s"has the text '${summaryMessages.yourTotalGain}'" in {
        div.select("div > h2").text shouldBe summaryMessages.yourTotalGain
      }

      "has a row for total gain" which {
        s"has the text '${summaryMessages.totalGain}'" in {
          doc.select("#totalGain-text").text shouldBe summaryMessages.totalGain
        }

        "has the value '£0'" in {
          doc.select("#totalGain-amount strong").text shouldBe "£0"
        }
      }
    }
  }

  "The shares were bought before 31st March 1982" should {

    val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(1000),
      worthWhenSoldForLess = None,
      disposalCosts = BigDecimal(100000),
      ownerBeforeLegislationStart = true,
      valueBeforeLegislationStart = Some(1000),
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(1000),
      acquisitionCosts = BigDecimal(100000)
    )

    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val view = gainSummaryPartialView(gainAnswers, taxYearModel, -100, 200000, 11000)(using fakeRequestWithSession, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "has a banner" which {
      lazy val banner = doc.select("#tax-owed-banner")

      "contains a h1" which {
        lazy val h1 = banner.select("h1")

        s"has the text ${summaryMessages.cgtToPay("2015 to 2016")}" in {
          h1.text() shouldEqual summaryMessages.cgtToPay("2015 to 2016")
        }
      }

      "contains a h2" which {
        lazy val h2 = banner.select("h2")

        s"has the text '£0.00'" in {
          h2.text() shouldEqual "£0.00"
        }
      }
    }

    "does not have a notice summary" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe true
    }

    "have a section for the Calculation details" which {

      "has a h2 tag" which {

        s"has the text '${summaryMessages.howWeWorkedThisOut}'" in {
          doc.select("#calcHeader").text shouldBe summaryMessages.howWeWorkedThisOut
        }
      }

      "has a div for total loss" which {

        lazy val div = doc.select("#yourTotalLoss").get(0)

        "has a caption tag" which {

          s"has the text '${summaryMessages.yourTotalLoss}'" in {
            div.select("div > h2").text shouldBe summaryMessages.yourTotalLoss
          }
        }

        "has a row for disposal value" which {
          s"has the text '${summaryMessages.disposalValue}'" in {
            doc.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
          }

          "has the value '£1,000'" in {
            doc.select("#disposalValue-amount span").text shouldBe "£1,000"
          }
        }

        "has a row for acquisition value" which {
          s"has the text '${summaryMessages.acquisitionValueBeforeLegislation}'" in {
            doc.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValueBeforeLegislation
          }

          "has the value '£1,000'" in {
            doc.select("#acquisitionValue-amount span").text shouldBe "£1,000"
          }
        }

        "has a row for total costs" which {
          s"has the text '${summaryMessages.totalCosts}'" in {
            doc.select("#totalCosts-text").text shouldBe summaryMessages.totalCosts
          }

          "has the value '£200,000'" in {
            doc.select("#totalCosts-amount").text shouldBe "£200,000"
          }
        }

        "has a row for total loss" which {
          s"has the text '${summaryMessages.totalLoss}'" in {
            doc.select("#totalLoss-text").text shouldBe summaryMessages.totalLoss
          }

          "has the value '£100'" in {
            doc.select("#totalLoss-amount strong").text shouldBe "£100"
          }
        }
      }

      "has a div for deductions" which {

        lazy val div = doc.select("#yourDeductions")

        "has a caption tag" which {

          s"has the text '${summaryMessages.yourDeductions}'" in {
            div.select("div > h2").text shouldBe summaryMessages.yourDeductions
          }
        }

        "not have a row for reliefs used" in {
          div.select("#reliefsUsed-text") shouldBe empty
        }

        "has a row for AEA used" which {

          s"has the text '${summaryMessages.aeaUsed}'" in {
            doc.select("#aeaUsed-text").text shouldBe summaryMessages.aeaUsed
          }

          "has the value '£0'" in {
            doc.select("#aeaUsed-amount span").text shouldBe "£0"
          }
        }

        "not have a row for brought forward losses used" in {
          doc.select("#lossesUsed-text") shouldBe empty
        }

        "has a row for total deductions" which {

          s"has the text '${summaryMessages.totalDeductions}'" in {
            doc.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
          }

          "has the value '£0'" in {
            doc.select("#totalDeductions-amount strong").text shouldBe "£0"
          }
        }
      }

      "has a div for Taxable Gain" which {

        lazy val div = doc.select("#yourTaxableGain")

        "does not have a caption tag" in {
          div.select("div > h2") shouldBe empty
        }

        "does not have a row for gain" in {
          div.select("#gain-text") shouldBe empty
        }

        "does not have a row for minus deductions" in {
          div.select("#minusDeductions-text") shouldBe empty
        }

        "has a row for taxable gain" which {
          s"has the text '${summaryMessages.taxableGain}'" in {
            doc.select("#taxableGain-text").text shouldBe summaryMessages.taxableGain
          }

          "has the value '£0'" in {
            doc.select("#taxableGain-amount strong").text shouldBe "£0"
          }
        }
      }

      "has a div for tax rate" which {

        lazy val div = doc.select("#yourTaxRate")

        "does not have a caption tag" in {
          div.select("div > h2") shouldBe empty
        }

        "does not have a row for first band"  in {
          div.select("#firstBand-text") shouldBe empty
        }

        "does not have a row for second band" in {
          div.select("#secondBand-text") shouldBe empty
        }

        "has a row for tax to pay" which {

          s"has the text ${summaryMessages.taxToPay}" in {
            doc.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
          }

          "has the value '£0'" in {
            doc.select("#taxToPay-amount strong").text shouldBe "£0"
          }
        }
      }
    }

    "have a section for the Your remaining deductions" which {

      "has a div for remaining deductions" which {

        lazy val div = doc.select("#remainingDeductions")

        "has a caption tag" which {

          s"has the text ${summaryMessages.remainingDeductions}" in {
            div.select("div > h2").text shouldBe summaryMessages.remainingDeductions
          }
        }

        "has a row for annual exempt amount left" which {
          s"has the text ${summaryMessages.remainingAnnualExemptAmount("2015 to 2016")}" in {
            doc.select("#aeaLeft-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2015 to 2016")
          }

          "has the value '£11,000'" in {
            doc.select("#aeaLeft-amount span").text shouldBe "£11,000"
          }
        }

        "not have a row for brought forward losses remaining" in {
          div.select("#broughtForwardLossesRemaining-text") shouldBe empty
        }

        "has a row for losses to carry forward" which {
          s"has the text${summaryMessages.lossesToCarryForwardFromCalculation}" in {
            doc.select("#lossesToCarryForwardFromCalc-text").text shouldBe summaryMessages.lossesToCarryForwardFromCalculation
          }

          "has the value '£100" in {
            doc.select("#lossesToCarryForwardFromCalc-amount span").text shouldBe "£100"
          }
        }
      }
    }
  }

  "The shares sold outside of known tax years" should {

    val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(100000),
      worthWhenSoldForLess = None,
      disposalCosts = BigDecimal(10000),
      ownerBeforeLegislationStart = true,
      valueBeforeLegislationStart = Some(1000),
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(0),
      acquisitionCosts = BigDecimal(100000)
    )

    val taxYearModel = TaxYearModel("2018/19", isValidYear = false, "2016/17")

    lazy val view = gainSummaryPartialView(gainAnswers, taxYearModel, -100, 200000, 11000)(using fakeRequestWithSession, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"display a notice summary with text ${summaryMessages.noticeSummary}" in {
      doc.select(".govuk-warning-text").text should include(summaryMessages.noticeSummary)
    }

  }

  "The shares were inherited" should {
    val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(100000),
      worthWhenSoldForLess = None,
      disposalCosts = BigDecimal(10000),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(true),
      worthWhenInherited = Some(1000),
      acquisitionValue = Some(0),
      acquisitionCosts = BigDecimal(100000)
    )

    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val view = gainSummaryPartialView(gainAnswers, taxYearModel, -100, 150, 11000)(using fakeRequestWithSession, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "has a row for acquisition value" which {
      s"has the text '${summaryMessages.acquisitionValue}'" in {
        doc.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
      }

      "has the value '£1,000'" in {
        doc.select("#acquisitionValue-amount span").text shouldBe "£1,000"
      }
    }
  }
}
