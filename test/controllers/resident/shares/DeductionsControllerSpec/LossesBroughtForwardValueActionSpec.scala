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

package controllers.resident.shares.DeductionsControllerSpec

import assets.MessageLookup.{LossesBroughtForwardValue => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.shares.DeductionsController
import models.resident._
import models.resident.shares._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class LossesBroughtForwardValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar{

  "Calling .lossesBroughtForwardValue from the resident DeductionsController" when {

    def setGetTarget(getData: Option[LossesBroughtForwardValueModel],
                     disposalDateModel: DisposalDateModel,
                     taxYearModel: TaxYearModel): DeductionsController = {

      val mockCalcConnector = mock[CalculatorConnector]

      when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](Matchers.eq(keystoreKeys.lossesBroughtForwardValue))
        (Matchers.any(), Matchers.any()))
        .thenReturn(getData)

      when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(keystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
        .thenReturn(Some(disposalDateModel))

      when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(taxYearModel)))

      new DeductionsController {
        override val calcConnector = mockCalcConnector
      }
    }

    "request has a valid session with no keystore data" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setGetTarget(None, disposalDateModel, taxYearModel)
      lazy val result = target.lossesBroughtForwardValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title("2015/16")}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title("2015/16")
      }

      s"have a back link to '${controllers.resident.shares.routes.DeductionsController.lossesBroughtForward().url}'" in {
        Jsoup.parse(bodyOf(result)).getElementById("back-link").attr("href") shouldEqual
          controllers.resident.shares.routes.DeductionsController.lossesBroughtForward().url
      }
    }

    "request has a valid session with some keystore data" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2014)
      lazy val taxYearModel = TaxYearModel("2014/15", false, "2015/16")
      lazy val target = setGetTarget(Some(LossesBroughtForwardValueModel(BigDecimal(1000))), disposalDateModel, taxYearModel)
      lazy val result = target.lossesBroughtForwardValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title("2015/15")}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title("2014/15")
      }
    }

    "request has an invalid session" should {
      lazy val result = DeductionsController.lossesBroughtForwardValue(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitLossesBroughtForwardValue from the resident DeductionsController" when {

    val gainModel = mock[GainAnswersModel]
    val summaryModel = mock[DeductionGainAnswersModel]

    def setPostTarget(otherPropertiesModel: Option[OtherPropertiesModel],
                      gainAnswers: GainAnswersModel,
                      chargeableGainAnswers: DeductionGainAnswersModel,
                      chargeableGain: ChargeableGainResultModel,
                      allowableLossesModel: Option[AllowableLossesModel] = None,
                      allowableLossesValueModel: Option[AllowableLossesValueModel] = None,
                      disposalDateModel: DisposalDateModel,
                      taxYearModel: TaxYearModel,
                      maxAnnualExemptAmount: Option[BigDecimal] = Some(BigDecimal(11100))
                     ): DeductionsController = {

      val mockCalcConnector = mock[CalculatorConnector]

      when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(keystoreKeys.otherProperties))(Matchers.any(), Matchers.any()))
        .thenReturn(otherPropertiesModel)

      when(mockCalcConnector.fetchAndGetFormData[AllowableLossesModel](Matchers.eq(keystoreKeys.allowableLosses))(Matchers.any(), Matchers.any()))
        .thenReturn(allowableLossesModel)

      when(mockCalcConnector.fetchAndGetFormData[AllowableLossesValueModel](Matchers.eq(keystoreKeys.allowableLossesValue))(Matchers.any(), Matchers.any()))
        .thenReturn(allowableLossesValueModel)

      when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(keystoreKeys.otherProperties))(Matchers.any(), Matchers.any()))
        .thenReturn(otherPropertiesModel)

      when(mockCalcConnector.getShareGainAnswers(Matchers.any()))
        .thenReturn(Future.successful(gainAnswers))

      when(mockCalcConnector.getShareDeductionAnswers(Matchers.any()))
        .thenReturn(Future.successful(chargeableGainAnswers))

      when(mockCalcConnector.calculateRttShareChargeableGain(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(chargeableGain)))

      when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(keystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
        .thenReturn(Some(disposalDateModel))

      when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(taxYearModel)))

      when(mockCalcConnector.getFullAEA(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(maxAnnualExemptAmount))

      new DeductionsController {
        override val calcConnector = mockCalcConnector
      }
    }

    "given a valid form" when {

      "the user has disposed of other properties with non-zero allowable losses" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(Some(OtherPropertiesModel(true)), gainModel, summaryModel,
          ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0), Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(1000))), disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.shares.routes.SummaryController.summary().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.shares.routes.SummaryController.summary().toString
        }
      }

      "the user has disposed of other properties with zero allowable losses" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(Some(OtherPropertiesModel(true)), gainModel, summaryModel,
          ChargeableGainResultModel(2000, 2000, 2000, 0, 2000, BigDecimal(0), BigDecimal(0), None, None, 0, 0), Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(0))), disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.shares.routes.DeductionsController.annualExemptAmount().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.shares.routes.DeductionsController.annualExemptAmount().toString
        }
      }

      "the user has disposed of other properties with no allowable losses" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(Some(OtherPropertiesModel(true)), gainModel, summaryModel,
          ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0), Some(AllowableLossesModel(false)),
          None, disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.shares.routes.DeductionsController.annualExemptAmount().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.shares.routes.DeductionsController.annualExemptAmount().toString
        }
      }

      "the user has not disposed of other properties and has zero chargeable gain" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(Some(OtherPropertiesModel(false)), gainModel, summaryModel,
          ChargeableGainResultModel(2000, 0, 0, 0, 2000, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
          disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.shares.routes.SummaryController.summary().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.shares.routes.SummaryController.summary().toString
        }
      }

      "the user has not disposed of other properties and has negative chargeable gain" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(Some(OtherPropertiesModel(false)), gainModel, summaryModel,
          ChargeableGainResultModel(2000, -1000, 0, 0, 3000, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
          disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.shares.routes.SummaryController.summary().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.shares.routes.SummaryController.summary().toString
        }
      }

      "the user has not disposed of other properties and has positive chargeable gain of £1,000" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(Some(OtherPropertiesModel(false)), gainModel, summaryModel,
          ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
          disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.shares.routes.SummaryController.summary().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.shares.routes.IncomeController.currentIncome().toString
        }
      }
    }

    "given an invalid form" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setPostTarget(Some(OtherPropertiesModel(false)), gainModel, summaryModel,
        ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
        disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitLossesBroughtForwardValue(request)

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      s"return a title of ${messages.title("2015/16")}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title("2015/16")
      }
    }
  }
}
