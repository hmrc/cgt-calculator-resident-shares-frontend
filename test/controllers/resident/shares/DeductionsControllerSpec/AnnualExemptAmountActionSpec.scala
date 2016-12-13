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

import assets.MessageLookup.{AnnualExemptAmount => messages}
import common.KeystoreKeys
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.shares.DeductionsController
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import scala.concurrent.Future

class AnnualExemptAmountActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  val gainModel = mock[GainAnswersModel]
  val summaryModel = mock[DeductionGainAnswersModel]
  val chargeableGainModel = mock[ChargeableGainResultModel]

  def setupTarget(getData: Option[AnnualExemptAmountModel],
                  gainAnswers: GainAnswersModel,
                  chargeableGainAnswers: DeductionGainAnswersModel,
                  chargeableGain: ChargeableGainResultModel,
                  maxAnnualExemptAmount: Option[BigDecimal] = Some(BigDecimal(11100)),
                  disposalDateModel: DisposalDateModel,
                  taxYearModel: TaxYearModel,
                  lossesBroughtForwardModel: LossesBroughtForwardModel): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[AnnualExemptAmountModel](Matchers.eq(keystoreKeys.annualExemptAmount))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[AnnualExemptAmountModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    when(mockCalcConnector.getShareGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.getShareDeductionAnswers(Matchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalcConnector.calculateRttShareChargeableGain(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(chargeableGain)))

    when(mockCalcConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(maxAnnualExemptAmount))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(disposalDateModel)))

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(taxYearModel)))

    when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardModel](Matchers.eq(keystoreKeys.lossesBroughtForward))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(lossesBroughtForwardModel)))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(keystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(DisposalDateModel(10, 10, 2015))))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .annualExemptAmount from the DeductionsController" when {

    "there is no keystore data" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val lossesBroughtForwardModel = LossesBroughtForwardModel(true)
      lazy val target = setupTarget(None, gainModel, summaryModel, chargeableGainModel, disposalDateModel = disposalDateModel,
        taxYearModel = taxYearModel, lossesBroughtForwardModel = lossesBroughtForwardModel)
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }
      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }
      "display the Annual Exempt Amount view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val lossesBroughtForwardModel = LossesBroughtForwardModel(false)
      lazy val target = setupTarget(Some(AnnualExemptAmountModel(1000)), gainModel, summaryModel, chargeableGainModel,
        disposalDateModel = disposalDateModel, taxYearModel = taxYearModel, lossesBroughtForwardModel = lossesBroughtForwardModel)
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Annual Exempt Amount view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "request has an invalid session" should {

      lazy val result = DeductionsController.annualExemptAmount(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitAnnualExemptAmount from the DeductionsController" when {

    "a valid form is submitted with AEA of 1000 and zero taxable gain" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val lossesBroughtForwardModel = LossesBroughtForwardModel(false)
      lazy val target = setupTarget(Some(AnnualExemptAmountModel(1000)), gainModel, summaryModel,
        ChargeableGainResultModel(0, 0, 1000, 0, 1000, BigDecimal(0), BigDecimal(0), None, None, 0, 0), disposalDateModel = disposalDateModel,
        taxYearModel = taxYearModel, lossesBroughtForwardModel = lossesBroughtForwardModel)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitAnnualExemptAmount(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/summary")
      }
    }

    "a valid form is submitted with AEA of 0 and positive taxable gain" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val lossesBroughtForwardModel = LossesBroughtForwardModel(false)
      lazy val target = setupTarget(Some(AnnualExemptAmountModel(0)), gainModel, summaryModel,
        ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0), disposalDateModel = disposalDateModel,
        taxYearModel = taxYearModel, lossesBroughtForwardModel = lossesBroughtForwardModel)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "0"))
      lazy val result = target.submitAnnualExemptAmount(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the previous taxable gains page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/previous-taxable-gains")
      }
    }

    "a valid form is submitted with AEA of 1000 and positive taxable gain" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val lossesBroughtForwardModel = LossesBroughtForwardModel(false)
      lazy val target = setupTarget(Some(AnnualExemptAmountModel(1000)), gainModel, summaryModel,
        ChargeableGainResultModel(2000, 1000, 1000, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0), disposalDateModel = disposalDateModel,
        taxYearModel = taxYearModel, lossesBroughtForwardModel = lossesBroughtForwardModel)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitAnnualExemptAmount(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/current-income")
      }
    }

    "an invalid form is submitted" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val lossesBroughtForwardModel = LossesBroughtForwardModel(false)
      lazy val target = setupTarget(None, gainModel, summaryModel, ChargeableGainResultModel(2000, 1000, 1000, 0, 0,
        BigDecimal(0), BigDecimal(0), None, None, 0, 0), disposalDateModel = disposalDateModel, taxYearModel = taxYearModel,
        lossesBroughtForwardModel = lossesBroughtForwardModel)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitAnnualExemptAmount(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the annual exempt amount page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}
