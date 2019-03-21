/*
 * Copyright 2019 HM Revenue & Customs
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

import assets.MessageLookup.{SummaryDetails => messages}
import common.Dates._
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import models.resident.shares.GainAnswersModel
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Lang
import play.api.i18n.Messages.Implicits._
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{report => views}

class SharesGainReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{

  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val fakeLang: Lang = Lang("en")

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
      lazy val view = views.gainSummaryReport(testModel, -2000, taxYearModel, 1000)(fakeRequest, mockMessage, fakeApplication, fakeLang)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      "have the hmrc logo with the hmrc name" in {
        doc.select("span.organisation-logo-text").text shouldBe "HM Revenue & Customs"
      }

      "have a banner for tax owed" in {
        doc.select("#tax-owed-banner").size() shouldBe 1
      }

      "have no tax year notice" in {
        doc.select("#notice-summary").size() shouldBe 0
      }

      "have a calculation details section" in {
        doc.select("#calcDetails").size() shouldBe 1
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
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel, 500)(fakeRequest, mockMessage, fakeApplication, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "have the class notice-wrapper" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe false
    }

    s"have the text ${messages.noticeSummary}" in {
      doc.select("strong.bold-small").text shouldBe messages.noticeSummary
    }
  }
}
