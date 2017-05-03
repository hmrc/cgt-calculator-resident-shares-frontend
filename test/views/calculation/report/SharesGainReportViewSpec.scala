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

package views.calculation.report

import assets.MessageLookup.Resident.{Shares => SharesMessages}
import assets.MessageLookup.{SummaryPage => messages}
import assets.{MessageLookup => commonMessages}
import common.Dates._
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import models.resident.shares.GainAnswersModel
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{report => views}

class SharesGainReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{

  "Summary view" when {

    "property acquired after start of tax (1 April 1982) and not inherited" should {

      val testModel = GainAnswersModel(
        disposalDate = constructDate(12, 9, 1990),
        soldForLessThanWorth = false,
        disposalValue = Some(10),
        worthWhenSoldForLess = None,
        disposalCosts = 20,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(30),
        acquisitionCosts = 40
      )

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = views.gainSummaryReport(testModel, -2000, taxYearModel)(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"have a page heading" which {

        s"includes a secondary heading with text '${messages.pageHeading}'" in {
          doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
        }

        "includes an amount of tax due of £0.00" in {
          doc.select("h1").text should include("£0.00")
        }
      }

      "have the hmrc logo with the hmrc name" in {
        doc.select("div.logo span").first().text shouldBe "HM Revenue & Customs"
      }

      "does not have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe true
      }

      s"have a section for the Calculation details" which {

        "has the class 'summary-section' to underline the heading" in {

          doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true

        }

        s"has a h2 tag" which {

          s"should have the title '${messages.calcDetailsHeadingDate("2015/16")}'" in {
            doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2015/16")
          }

          "has the class 'heading-large'" in {
            doc.select("section#calcDetails h2").hasClass("heading-large") shouldBe true
          }
        }

        "has a numeric output row for the gain" which {

          "should have the question text 'Loss'" in {
            doc.select("#gain-question").text shouldBe messages.totalLoss
          }

          "should have the value '£2,000'" in {
            doc.select("#gain-amount").text shouldBe "£2,000"
          }
        }
      }

      s"have a section for Your answers" which {

        "has an entry for disposal date" in {
          doc.select("#disposalDate-question").size() shouldBe 1
        }

        "has no entry for brought forward losses" in {
          doc.select("#broughtForwardLosses-question").size() shouldBe 0
        }

        "has no entry for personal allowance" in {
          doc.select("#personalAllowance-question").size() shouldBe 0
        }
      }
    }
  }

  "Summary when supplied with a date outside the known tax years and no gain or loss" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(10),
      worthWhenSoldForLess = None,
      disposalCosts = 20,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(30),
      acquisitionCosts = 40
    )
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "should have the question text 'Total gain'" in {
      doc.select("#gain-question").text shouldBe messages.totalGain
    }

    "have the class notice-wrapper" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe false
    }

    s"have the text ${messages.noticeWarning("2016/17")}" in {
      doc.select("strong.bold-small").text shouldBe messages.noticeWarning("2016/17")
    }
  }
}
