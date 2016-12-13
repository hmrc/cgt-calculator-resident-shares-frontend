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

package controllers.resident.shares.IncomeControllerSpec

import assets.MessageLookup.{CurrentIncome => messages}
import common.Dates
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.shares.IncomeController
import models.resident._
import models.resident.income._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class CurrentIncomeActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(storedData: Option[CurrentIncomeModel],
                  otherProperties: Boolean = true,
                  lossesBroughtForward: Boolean = true,
                  annualExemptAmount: BigDecimal = 0,
                  allowableLossesModel: Option[AllowableLossesModel] = None,
                  allowableLossesValueModel: Option[AllowableLossesValueModel] = None,
                  disposalDate: Option[DisposalDateModel],
                  taxYear: Option[TaxYearModel]): IncomeController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](Matchers.eq(keystoreKeys.currentIncome))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(storedData))

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(keystoreKeys.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(OtherPropertiesModel(otherProperties))))

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesModel](Matchers.eq(keystoreKeys.allowableLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(allowableLossesModel))

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesValueModel](Matchers.eq(keystoreKeys.allowableLossesValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(allowableLossesValueModel))

    when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardModel](Matchers.eq(keystoreKeys.lossesBroughtForward))(Matchers.any(),
      Matchers.any()))
      .thenReturn(Future.successful(Some(LossesBroughtForwardModel(lossesBroughtForward))))

    when(mockCalcConnector.fetchAndGetFormData[AnnualExemptAmountModel](Matchers.eq(keystoreKeys.annualExemptAmount))(Matchers.any(),
      Matchers.any()))
      .thenReturn(Future.successful(Some(AnnualExemptAmountModel(annualExemptAmount))))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(keystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(disposalDate)

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(taxYear)

    new IncomeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .currentIncome from the IncomeController with a session" when {

    "supplied with no pre-existing stored data for 2015/16" should {

      lazy val target = setupTarget(None, disposalDate = Some(DisposalDateModel(10, 10, 2015)), taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Current Income view for 2015/16" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title("2015/16")
      }

      "supplied with no pre-existing stored data for 2016/17" should {

        lazy val target = setupTarget(None, disposalDate = Some(DisposalDateModel(10, 10, 2016)), taxYear =
          Some(TaxYearModel(Dates.getCurrentTaxYear, true, Dates.getCurrentTaxYear)))
        lazy val result = target.currentIncome(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        "return some html" in {
          contentType(result) shouldBe Some("text/html")
        }

        "display the Current Income for 2016/17 view" in {
          Jsoup.parse(bodyOf(result)).title shouldBe messages.currentYearTitle
        }
      }
    }

    "supplied with pre-existing stored data" should {

      lazy val target = setupTarget(Some(CurrentIncomeModel(40000)), disposalDate = Some(DisposalDateModel(10, 10, 2015)),
        taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "have the amount 40000 pre-populated into the input field" in {
        doc.getElementById("amount").attr("value") shouldBe "40000"
      }
    }

    "other shares have been selected and 0 has been entered into the annual exempt amount" should {

      lazy val target = setupTarget(None, allowableLossesModel = Some(AllowableLossesModel(true)), allowableLossesValueModel =
        Some(AllowableLossesValueModel(BigDecimal(0))), disposalDate = Some(DisposalDateModel(10, 10, 2015)),
        taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/previous-taxable-gains" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/previous-taxable-gains"
      }
    }

    "other shares have been selected and non-zero has been entered into the annual exempt amount" should {

      lazy val target = setupTarget(None, annualExemptAmount = 10, allowableLossesModel = Some(AllowableLossesModel(false)),
                                    allowableLossesValueModel = Some(AllowableLossesValueModel(BigDecimal(0))),
                                    disposalDate = Some(DisposalDateModel(10, 10, 2015)), taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/annual-exempt-amount" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
      }
    }

    "other shares has been selected but a non-zero value for allowable losses has been entered" should {

      lazy val target = setupTarget(None, otherProperties = true, lossesBroughtForward = false, allowableLossesModel = Some(AllowableLossesModel(true)),
                                    allowableLossesValueModel = Some(AllowableLossesValueModel(BigDecimal(10000))),
                                    disposalDate = Some(DisposalDateModel(10, 10, 2015)), taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward"
      }
    }

    "other shares has not been selected and neither has brought forward losses" should {

      lazy val target = setupTarget(None, otherProperties = false, lossesBroughtForward = false, disposalDate = Some(DisposalDateModel(10, 10, 2015)),
                                    taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward"
      }
    }

    "other shares has not been selected and brought forward losses has been selected" should {

      lazy val target = setupTarget(None, otherProperties = false, disposalDate = Some(DisposalDateModel(10, 10, 2015)),
                                    taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/losses-brought-forward-value" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward-value"
      }
    }
  }

  "Calling .currentIncome from the IncomeController with no session" should {

    lazy val result = IncomeController.currentIncome(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
    }
  }

  "calling .submitCurrentIncome from the IncomeController" when {

    "given a valid form should" should {

      lazy val target = setupTarget(Some(CurrentIncomeModel(40000)), disposalDate = Some(DisposalDateModel(10, 10, 2015)),
                                    taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.submitCurrentIncome(fakeRequestToPOSTWithSession(("amount", "40000")))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.resident.shares.routes.IncomeController.personalAllowance().toString}'" in {
        redirectLocation(result).get shouldBe controllers.resident.shares.routes.IncomeController.personalAllowance().toString
      }
    }

    "given an invalid form" should {

      lazy val target = setupTarget(Some(CurrentIncomeModel(-40000)), otherProperties = false,
                                    disposalDate = Some(DisposalDateModel(10, 10, 2015)), taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.submitCurrentIncome(fakeRequestToPOSTWithSession(("amount", "-40000")))

      "return a status of 400" in {
        status(result) shouldBe 400
      }
    }
  }
}
