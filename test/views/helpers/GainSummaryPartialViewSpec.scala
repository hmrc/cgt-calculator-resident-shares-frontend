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

import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares.GainAnswersModel
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits.applicationMessages
import assets.MessageLookup.{SummaryDetails => summaryMessages}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class GainSummaryPartialViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "the shares were sold for less than worth" should {

    val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(100),
      worthWhenSoldForLess = None,
      disposalCosts = BigDecimal(100000),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(10000),
      acquisitionCosts = BigDecimal(100000)
    )

    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val view = views.gainSummaryPartial(gainAnswers, taxYearModel, -100, 150, 11000)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "has a banner" which {
      lazy val banner = doc.select("#tax-owed-banner")

      "contains a h1" which {
        lazy val h1 = banner.select("h1")

        s"has the text '£0.00'" in {
          h1.text() shouldEqual "£0.00"
        }
      }

      "contains a h2" which {
        lazy val h2 = banner.select("h2")

        s"has the text ${summaryMessages.cgtToPay("2015 to 2016")}" in {
          h2.text() shouldEqual summaryMessages.cgtToPay("2015 to 2016")
        }
      }
    }

    "does not have a notice summary" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe true
    }

    "have a section for the Calculation details" which {

      "has a h2 tag" which {

        s"has the text '${summaryMessages.howWeWorkedThisOut}'" in {
          doc.select("section#calcDetails h2").text shouldBe summaryMessages.howWeWorkedThisOut
        }
      }

      "has a div for total loss" which {

        lazy val div = doc.select("#yourTotalLoss").get(0)

        "has a h3 tag" which {

          s"has the text '${summaryMessages.yourTotalLoss}'" in {
            div.select("h3").text shouldBe summaryMessages.yourTotalLoss
          }
        }

        "has a row for disposal value" which {
          s"has the text '${summaryMessages.disposalValue}'" in {
            div.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
          }

          "has the value '£10,000'" in {
            div.select("#disposalValue-amount").text shouldBe "£10,000"
          }
        }

        "has a row for acquisition value" which {
          s"has the text '${summaryMessages.acquisitionValue}'" in {
            div.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
          }

          "has the value '£10,000'" in {
            div.select("#acquisitionValue-amount").text shouldBe "£10,000"
          }
        }

        "has a row for total costs" which {
          s"has the text '${summaryMessages.totalCosts}'" in {
            div.select("#totalCosts-text").text shouldBe summaryMessages.totalCosts
          }

          "has the value '£150'" in {
            div.select("#totalCosts-amount").text shouldBe "£150"
          }
        }

        "has a row for total loss" which {
          s"has the text '${summaryMessages.totalLoss}'" in {
            div.select("#totalLoss-text").text shouldBe summaryMessages.totalLoss
          }

          "has the value '£100'" in {
            div.select("#totalLoss-amount").text shouldBe "£100"
          }
        }
      }

      "has a div for deductions" which {

        lazy val div = doc.select("#yourDeductions")

        "has a h3 tag" which {

          s"has the text '${summaryMessages.yourDeductions}'" in {
            div.select("h3").text shouldBe summaryMessages.yourDeductions
          }
        }

        "not have a row for reliefs used" in {
          div.select("#reliefsUsed-text") shouldBe empty
        }

        "has a row for AEA used" which {

          s"has the text '${summaryMessages.aeaUsed}'" in {
            div.select("#aeaUsed-text").text shouldBe summaryMessages.aeaUsed
          }

          "has the value '£0'" in {
            div.select("#aeaUsed-amount").text shouldBe "£0"
          }
        }

        "not have a row for brought forward losses used" in {
          div.select("#lossesUsed-text") shouldBe empty
        }

        "has a row for total deductions" which {

          s"has the text '${summaryMessages.totalDeductions}'" in {
            div.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
          }

          "has the value '£0'" in {
            div.select("#totalDeductions-amount").text shouldBe "£0"
          }
        }
      }

      "has a div for Taxable Gain" which {

        lazy val div = doc.select("#yourTaxableGain")

        "does not have a h3 tag" in {
          div.select("h3") shouldBe empty
        }

        "does not have a row for gain" in {
          div.select("#gain-text") shouldBe empty
        }

        "does not have a row for minus deductions" in {
          div.select("#minusDeductions-text") shouldBe empty
        }

        "has a row for taxable gain" which {
          s"has the text '${summaryMessages.taxableGain}'" in {
            div.select("#taxableGain-text").text shouldBe summaryMessages.taxableGain
          }

          "has the value '£0'" in {
            div.select("#taxableGain-amount").text shouldBe "£0"
          }
        }
      }

      "has a div for tax rate" which {

        lazy val div = doc.select("#yourTaxRate")

        "does not have a h3 tag" in {
          div.select("h3") shouldBe empty
        }

        "does not have a row for first band"  in {
          div.select("#firstBand-text") shouldBe empty
        }

        "does not have a row for second band" in {
          div.select("#secondBand-text") shouldBe empty
        }

        "has a row for tax to pay" which {

          s"has the text ${summaryMessages.taxToPay}" in {
            div.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
          }

          "has the value '£0'" in {
            div.select("#taxToPay-amount").text shouldBe "£0"
          }
        }
      }
    }

    "have a section for the Your remaining deductions" which {

      "has a div for remaining deductions" which {

        lazy val div = doc.select("#remainingDeductions")

        "has a h2 tag" which {

          s"has the text ${summaryMessages.remainingDeductions}" in {
            div.select("h2").text shouldBe summaryMessages.remainingDeductions
          }
        }

        "has a row for annual exempt amount left" which {
          s"has the text ${summaryMessages.remainingAnnualExemptAmount("2015 to 2016")}" in {
            div.select("#aeaLeft-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2015 to 2016")
          }

          "has the value '£11,000'" in {
            div.select("#aeaLeft-amount").text shouldBe "£11,000"
          }
        }

        "not have a row for brought forward losses remaining" in {
          div.select("#broughtForwardLossesRemaining-text") shouldBe empty
        }

        "has a row for losses to carry forward" which {
          s"has the text${summaryMessages.lossesToCarryForwardFromCalculation}" in {
            div.select("#lossesToCarryForwardFromCalc-text").text shouldBe summaryMessages.lossesToCarryForwardFromCalculation
          }

          "has the value '£100" in {
            div.select("#lossesToCarryForwardFromCalc-amount").text shouldBe "£100"
          }
        }
      }
    }
  }
}
