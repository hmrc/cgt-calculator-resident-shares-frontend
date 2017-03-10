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

package controllers.DeductionsControllerSpec


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import assets.MessageLookup.{LossesBroughtForward => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.DeductionsController
import models.resident.{OtherPropertiesModel, _}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class LossesBroughtForwardActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem = ActorSystem()
  implicit lazy val mat = ActorMaterializer()

  val gainModel = mock[GainAnswersModel]
  val summaryModel = mock[DeductionGainAnswersModel]
  val chargeableGainModel = mock[ChargeableGainResultModel]

  def setupTarget(lossesBroughtForwardData: Option[LossesBroughtForwardModel],
                  otherPropertiesData: Option[OtherPropertiesModel],
                  allowableLossesData: Option[AllowableLossesModel],
                  gainAnswers: GainAnswersModel,
                  chargeableGainAnswers: DeductionGainAnswersModel,
                  chargeableGain: ChargeableGainResultModel,
                  allowableLossesValueModel: Option[AllowableLossesValueModel], disposalDate: Option[DisposalDateModel],
                  taxYear: Option[TaxYearModel], maxAnnualExemptAmount: Option[BigDecimal] = Some(BigDecimal(11100))): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardModel](ArgumentMatchers.eq(keystoreKeys.lossesBroughtForward))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(lossesBroughtForwardData))

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](ArgumentMatchers.eq(keystoreKeys.otherProperties))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(otherPropertiesData))

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesModel](ArgumentMatchers.eq(keystoreKeys.allowableLosses))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(allowableLossesData))

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesValueModel](ArgumentMatchers.eq(keystoreKeys.allowableLossesValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(allowableLossesValueModel))

    when(mockCalcConnector.getShareGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.getShareDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalcConnector.calculateRttShareChargeableGain(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(chargeableGain)))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(disposalDate)

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(taxYear)

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(maxAnnualExemptAmount))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .lossesBroughtForward from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(false)), None, gainModel, summaryModel,
        chargeableGainModel, None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title("2015/16")}" in {
        doc.title shouldEqual messages.title("2015/16")
      }
    }

    "request has no session" should {

      lazy val target = setupTarget(None, None, None, gainModel, summaryModel, chargeableGainModel, None, None, None, None)
      lazy val result = target.lossesBroughtForward(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }
    }

    "no other properties have been selected" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(false)), None, gainModel, summaryModel,
        chargeableGainModel, None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/other-disposals" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/other-disposals"
      }
    }

    "other properties have been selected" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(true)), None, gainModel, summaryModel,
        chargeableGainModel, None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/allowable-losses" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/allowable-losses"
      }
    }

    "other properties have been selected but no allowable losses" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(false)),
        gainModel, summaryModel, chargeableGainModel, None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/allowable-losses" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/allowable-losses"
      }
    }

    "other properties have been selected and allowable losses are claimed" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(true)),
        gainModel, summaryModel, chargeableGainModel, None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/shares/allowable-losses-value" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/allowable-losses-value"
      }
    }
  }

  "Calling .submitLossesBroughtForward from the DeductionsController" when {

    "a valid form 'No' and no other properties are claimed and chargeable gain is £1000" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), Some(OtherPropertiesModel(false)),
        None, gainModel, summaryModel, ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0),
          None, None, 0, 0), None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)


      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the current income page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/current-income")
      }
    }

    "a valid form 'No' and no other properties are claimed and chargeable gain is zero" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), Some(OtherPropertiesModel(false)),
        None, gainModel, summaryModel, ChargeableGainResultModel(1000, 0, 0, 0, 1000, BigDecimal(0), BigDecimal(0),
          None, None, 0, 0), None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/summary")
      }
    }

    "a valid form 'No' and no other properties are claimed and has a positive chargeable gain of £1,000" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), Some(OtherPropertiesModel(false)),
        None, gainModel, summaryModel, ChargeableGainResultModel(1000, -1000, 0, 0, 2000, BigDecimal(0), BigDecimal(0),
          None, None, 0, 0), None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/summary")
      }
    }

    "a valid form 'No' is and other properties are claimed with non-zero allowable losses" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), Some(OtherPropertiesModel(true)),
        Some(AllowableLossesModel(true)), gainModel, summaryModel, ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0),
          BigDecimal(0), None, None, 0, 0), Some(AllowableLossesValueModel(1000)), Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/summary")
      }
    }

    "a valid form 'No' is and other properties are claimed with zero allowable losses" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), Some(OtherPropertiesModel(true)),
        Some(AllowableLossesModel(true)), gainModel, summaryModel, ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0),
          BigDecimal(0), None, None, 0, 0), Some(AllowableLossesValueModel(0)), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the annual exempt amount page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/annual-exempt-amount")
      }
    }

    "a valid form 'Yes' is and other properties are not claimed" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), Some(OtherPropertiesModel(false)), None,
        gainModel, summaryModel, ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0), None,
        Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "Yes"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/losses-brought-forward-value")
      }
    }

    "a valid form 'Yes' is and other properties are claimed" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), Some(OtherPropertiesModel(true)),
        Some(AllowableLossesModel(false)), gainModel, summaryModel, ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0),
          BigDecimal(0), None, None, 0, 0), None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "Yes"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/losses-brought-forward-value")
      }
    }

    "an invalid form is submitted" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), Some(OtherPropertiesModel(false)), None,
        gainModel, summaryModel, chargeableGainModel, None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", ""))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the brought forward losses page" in {
        Jsoup.parse(bodyOf(result)).title() shouldEqual messages.title("2015/16")
      }
    }
  }
}
