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

package views.calculation.summary

import common.Dates
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models.resident._
import models.resident.shares._
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{summary => views}

class SharesDeductionsSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

//  "Properties Deductions Summary view" should {
//    val gainAnswers = YourAnswersSummaryModel(
//      disposalDate = Dates.constructDate(10, 10, 2015),
//      disposalValue = Some(100000),
//      worthWhenSoldForLess = None,
//      whoDidYouGiveItTo = None,
//      worthWhenGaveAway = None,
//      disposalCosts = BigDecimal(10000),
//      acquisitionValue = Some(0),
//      worthWhenInherited = None,
//      worthWhenGifted = None,
//      worthWhenBoughtForLess = None,
//      acquisitionCosts = BigDecimal(10000),
//      improvements = BigDecimal(30000),
//      givenAway = false,
//      sellForLess = Some(false),
//      ownerBeforeLegislationStart = false,
//      valueBeforeLegislationStart = None,
//      howBecameOwner = Some("Bought"),
//      boughtForLessThanWorth = Some(false)
//    )
//    val deductionAnswers = ChargeableGainAnswers(
//      broughtForwardModel = Some(LossesBroughtForwardModel(false)),
//      broughtForwardValueModel = None,
//      propertyLivedInModel = Some(PropertyLivedInModel(false)),
//      privateResidenceReliefModel = None,
//      privateResidenceReliefValueModel = None,
//      lettingsReliefModel = None,
//      lettingsReliefValueModel = None
//    )
//    val results = ChargeableGainResultModel(
//      gain = 30000,
//      chargeableGain = 0,
//      aeaUsed = 900,
//      aeaRemaining = 1000,
//      deductions = 30000,
//      allowableLossesRemaining = 0,
//      broughtForwardLossesRemaining = 0,
//      lettingReliefsUsed = Some(BigDecimal(0)),
//      prrUsed = Some(BigDecimal(0)),
//      broughtForwardLossesUsed = 0,
//      allowableLossesUsed = 0
//    )
//    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")
//
//    val backUrl = controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url
//
//    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backUrl,
//      taxYearModel, None, None, 100)(fakeRequestWithSession, applicationMessages)
//    lazy val doc = Jsoup.parse(view.body)
//
//    "have a charset of UTF-8" in {
//      doc.charset().toString shouldBe "UTF-8"
//    }
//
//    s"have a title ${messages.title}" in {
//      doc.title() shouldBe messages.title
//    }
//
//    s"have a back button" which {
//
//      lazy val backLink = doc.getElementById("back-link")
//
//      "has the id 'back-link'" in {
//        backLink.attr("id") shouldBe "back-link"
//      }
//
//      s"has the text '${residentMessages.back}'" in {
//        backLink.text shouldBe residentMessages.back
//      }
//
//      s"has a link to '${controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url}'" in {
//        backLink.attr("href") shouldBe controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url
//      }
//    }
//
//    "has a banner" which {
//      lazy val banner = doc.select("#tax-owed-banner")
//
//      "contains a h1" which {
//        lazy val h1 = banner.select("h1")
//
//        s"has the text '£0.00'" in {
//          h1.text() shouldEqual "£0.00"
//        }
//      }
//
//      "contains a h2" which {
//        lazy val h2 = banner.select("h2")
//
//        s"has the text ${summaryMessages.cgtToPay("2015 to 2016")}" in {
//          h2.text() shouldEqual summaryMessages.cgtToPay("2015 to 2016")
//        }
//      }
//    }
//
//    "does not have a notice summary" in {
//      doc.select("div.notice-wrapper").isEmpty shouldBe true
//    }
//
//    "have a section for the Calculation details" which {
//
//      "has a h2 tag" which {
//
//        s"has the text '${summaryMessages.howWeWorkedThisOut}'" in {
//          doc.select("section#calcDetails h2").text shouldBe summaryMessages.howWeWorkedThisOut
//        }
//      }
//
//      "has a div for total gain" which {
//
//        lazy val div = doc.select("#yourTotalGain")
//
//        "has a h3 tag" which {
//
//          s"has the text '${summaryMessages.yourTotalGain}'" in {
//            div.select("h3").text shouldBe summaryMessages.yourTotalGain
//          }
//        }
//
//        "has a row for disposal value" which {
//
//          s"has the text '${summaryMessages.disposalValue}'" in {
//            div.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
//          }
//
//          "has the value '£100,000'" in {
//            div.select("#disposalValue-amount").text shouldBe "£100,000"
//          }
//        }
//
//        "has a row for acquisition value" which {
//          s"has the text '${summaryMessages.acquisitionValue}'" in {
//            div.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
//          }
//
//          "has the value '£0'" in {
//            div.select("#acquisitionValue-amount").text shouldBe "£0"
//          }
//        }
//
//        "has a row for total costs" which {
//          s"has the text '${summaryMessages.totalCosts}'" in {
//            div.select("#totalCosts-text").text shouldBe summaryMessages.totalCosts
//          }
//
//          "has the value '£100'" in {
//            div.select("#totalCosts-amount").text shouldBe "£100"
//          }
//        }
//
//        "has a row for total gain" which {
//          s"has the text '${summaryMessages.totalGain}'" in {
//            div.select("#totalGain-text").text shouldBe summaryMessages.totalGain
//          }
//
//          "has the value '£30,000'" in {
//            div.select("#totalGain-amount").text shouldBe "£30,000"
//          }
//        }
//      }
//
//      "has a div for deductions" which {
//
//        lazy val div = doc.select("#yourDeductions")
//
//        "has a h3 tag" which {
//
//          s"has the text '${summaryMessages.yourDeductions}'" in {
//            div.select("h3").text shouldBe summaryMessages.yourDeductions
//          }
//        }
//
//        "not have a row for reliefs used" in {
//          div.select("#reliefsUsed-text") shouldBe empty
//        }
//
//        "has a row for AEA used" which {
//
//          s"has the text '${summaryMessages.aeaUsed}'" in {
//            div.select("#aeaUsed-text").text shouldBe summaryMessages.aeaUsed
//          }
//
//          "has the value '£900'" in {
//            div.select("#aeaUsed-amount").text shouldBe "£900"
//          }
//        }
//
//        "not have a row for brought forward losses used" in {
//          div.select("#lossesUsed-text") shouldBe empty
//        }
//
//        "has a row for total deductions" which {
//
//          s"has the text '${summaryMessages.totalDeductions}'" in {
//            div.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
//          }
//
//          "has the value '£30,000'" in {
//            div.select("#totalDeductions-amount").text shouldBe "£30,000"
//          }
//        }
//      }
//
//      "has a div for Taxable Gain" which {
//
//        lazy val div = doc.select("#yourTaxableGain")
//
//        "has a h3 tag" which {
//
//          s"has the text '${summaryMessages.yourTaxableGain}'" in {
//            div.select("h3").text shouldBe summaryMessages.yourTaxableGain
//          }
//        }
//
//        "has a row for gain" which {
//          s"has the text '${summaryMessages.totalGain}'" in {
//            div.select("#gain-text").text shouldBe summaryMessages.totalGain
//          }
//
//          "has the value '£30,000'" in {
//            div.select("#gain-amount").text shouldBe "£30,000"
//          }
//        }
//
//        "has a row for minus deductions" which {
//          s"has the text '${summaryMessages.minusDeductions}'" in {
//            div.select("#minusDeductions-text").text shouldBe summaryMessages.minusDeductions
//          }
//
//          "has the value '£30,000'" in {
//            div.select("#minusDeductions-amount").text shouldBe "£30,000"
//          }
//        }
//
//        "has a row for taxable gain" which {
//          s"has the text '${summaryMessages.taxableGain}'" in {
//            div.select("#taxableGain-text").text shouldBe summaryMessages.taxableGain
//          }
//
//          "has the value '£0'" in {
//            div.select("#taxableGain-amount").text shouldBe "£0"
//          }
//        }
//      }
//
//      "has a div for tax rate" which {
//
//        "has a row for tax to pay" which {
//
//          s"has the text ${summaryMessages.taxToPay}" in {
//            doc.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
//          }
//
//          "has the value '£0'" in {
//            doc.select("#taxToPay-amount").text shouldBe "£0"
//          }
//        }
//      }
//    }
//
//    "have a section for the Your remaining deductions" which {
//
//      "has a div for remaining deductions" which {
//
//        lazy val div = doc.select("#remainingDeductions")
//
//        "has a h2 tag" which {
//
//          s"has the text ${summaryMessages.remainingDeductions}" in {
//            div.select("h2").text shouldBe summaryMessages.remainingDeductions
//          }
//        }
//
//        "has a row for annual exempt amount left" which {
//          s"has the text ${summaryMessages.remainingAnnualExemptAmount("2015 to 2016")}" in {
//            div.select("#aeaRemaining-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2015 to 2016")
//          }
//
//          "has the value '£1,000'" in {
//            div.select("#aeaRemaining-amount").text shouldBe "£1,000"
//          }
//        }
//
//        "not have a row for brought forward losses remaining" in {
//          div.select("#broughtForwardLossesRemaining-text") shouldBe empty
//        }
//
//        "not have a row for losses to carry forward" in {
//          div.select("#lossesToCarryForward-text") shouldBe empty
//        }
//      }
//    }
//  }
}
