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

import assets.{MessageLookup => pages}
import assets.MessageLookup.{SummaryPage => messages}
import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.Resident.{Shares => SharesMessages}
import common.Dates._
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models.resident.TaxYearModel
import models.resident.shares.GainAnswersModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{summary => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SharesGainSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Summary view" when {

    "acquired after start of tax (1 April 1982) and not inherited" should {

      val testModel = GainAnswersModel(
        disposalDate = constructDate(12, 12, 2019),
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

      lazy val taxYearModel = TaxYearModel("2019/20", false, "2016/17")
      lazy val view = views.gainSummary(testModel, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"have a back button" which {

        lazy val backLink = doc.getElementById("back-link")

        "has the id 'back-link'" in {
          backLink.attr("id") shouldBe "back-link"
        }

        s"has the text '${commonMessages.back}'" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has the url ${routes.ReviewAnswersController.reviewGainAnswers().toString}" in {
          backLink.attr("href") shouldEqual routes.ReviewAnswersController.reviewGainAnswers().toString
        }
      }

      "have a home link to 'home-link'" in {
        doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
      }

      s"have a page heading" which {

        s"includes a secondary heading with text '${messages.pageHeading}'" in {
          doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
        }

        "includes an amount of tax due of £0.00" in {
          doc.select("h1").text should include("£0.00")
        }
      }

      "have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe false
      }

      s"have a section for the Calculation details" which {

        "has the class 'summary-section' to underline the heading" in {

          doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true

        }

        s"has a h2 tag" which {

          s"should have the title '${messages.calcDetailsHeadingDate("2015/16")}'" in {
            doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2019/20")
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
    }
  }

  "Summary when supplied with a date within the known tax years and a loss" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

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
    lazy val view = views.gainSummary(testModel, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("#whatToDoNextTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextText}" in {
      doc.select("#whatToDoNext p").text shouldEqual messages.whatToDoNextText
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-link").size() shouldEqual 1
      }

      s"with an href to ${routes.ReportController.gainSummaryReport().toString}" in {
        doc.select("a.save-pdf-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/gain-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-link").text shouldEqual messages.saveAsPdf
      }
    }

    "has a continue button" which {
      s"has the text ${commonMessages.continue}" in {
        doc.select("button").text shouldBe commonMessages.continue
      }
    }
  }

  "Summary when supplied with a date within the known tax years and no gain or loss" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

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
    lazy val view = views.gainSummary(testModel, 0, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("h2#whatToDoNextTitle").text shouldEqual messages.whatToDoNextTitle
    }
  }

  "Summary when supplied with a date above the known tax years" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2018),
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
    lazy val view = views.gainSummary(testModel,-2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "does not display the what to do next content" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }
  }

  "Summary view with an out of tax year date" should {

    val testModel = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2013),
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

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val view = views.gainSummary(testModel, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have the class notice-wrapper" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe false
    }

    s"have the text ${messages.noticeWarning("2015/16")}" in {
      doc.select("strong.bold-small").text shouldBe messages.noticeWarning("2015/16")
    }

    "have a warning icon" in {
      doc.select("i.icon-important").isEmpty shouldBe false
    }

    "have a visually hidden warning text" in {
      doc.select("div.notice-wrapper span.visuallyhidden").text shouldBe messages.warning
    }

    s"has a h2 tag" which {

      s"should have the title '${messages.calcDetailsHeadingDate("2013/14")}'" in {
        doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2013/14")
      }
    }
  }
}
