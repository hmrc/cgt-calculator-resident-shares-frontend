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

package controllers.IncomeControllerSpec

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import controllers.helpers.FakeRequestHelper
import controllers.IncomeController
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{PreviousTaxableGains => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystore}
import connectors.CalculatorConnector
import models.resident._
import models.resident.income.PreviousTaxableGainsModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class PreviousTaxableGainsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem = ActorSystem()
  implicit lazy val mat = ActorMaterializer()

  def setupTarget(getData: Option[PreviousTaxableGainsModel], otherProperties: Boolean = true, lossesBroughtForward: Boolean = true,
                  allowableLossesModel: Option[AllowableLossesModel] = None, allowableLossesValueModel: Option[AllowableLossesValueModel] = None,
                  taxYearModel: TaxYearModel = TaxYearModel("2015/16", true, "2015/16"),
                  disposalDateModel: DisposalDateModel = DisposalDateModel(12, 12, 2015)): IncomeController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardModel](ArgumentMatchers.eq(keystore.lossesBroughtForward))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(LossesBroughtForwardModel(lossesBroughtForward))))

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](ArgumentMatchers.eq(keystore.otherProperties))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(OtherPropertiesModel(otherProperties))))

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesModel](ArgumentMatchers.eq(keystore.allowableLosses))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(allowableLossesModel))

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesValueModel](ArgumentMatchers.eq(keystore.allowableLossesValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(allowableLossesValueModel))

    when(mockCalcConnector.fetchAndGetFormData[PreviousTaxableGainsModel](ArgumentMatchers.eq(keystore.previousTaxableGains))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystore.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(disposalDateModel)))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(taxYearModel)))

    new IncomeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .previousTaxableGains from the IncomeController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.previousTaxableGains(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the previous taxable gains view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title("2015/16")
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(PreviousTaxableGainsModel(1000)))
      lazy val result = target.previousTaxableGains(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Improvements view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title("2015/16")
      }
    }

    "other shares have been specified" should {
      "return a back link to the AEA page" in {
        val target = setupTarget(Some(PreviousTaxableGainsModel(1000)), otherProperties = true,
          allowableLossesModel = Some(AllowableLossesModel(false)), allowableLossesValueModel = None)
        val result = target.previousTaxableGains(fakeRequestWithSession)
        val doc = Jsoup.parse(bodyOf(result))

        val link = doc.select("#back-link")
        link.attr("href") shouldBe controllers.routes.DeductionsController.annualExemptAmount().toString
      }
    }

    "other shares have been specified with an allowable loss of 0" should {
      "return a back link to the AEA page" in {
        val target = setupTarget(Some(PreviousTaxableGainsModel(1000)), otherProperties = true,
          allowableLossesModel = Some(AllowableLossesModel(true)), allowableLossesValueModel = Some(AllowableLossesValueModel(BigDecimal(0))))
        val result = target.previousTaxableGains(fakeRequestWithSession)
        val doc = Jsoup.parse(bodyOf(result))

        val link = doc.select("#back-link")
        link.attr("href") shouldBe controllers.routes.DeductionsController.annualExemptAmount().toString
      }
    }

    "other shares have been specified with a non-zero allowable loss" should {
      "return a back link to the losses brought forward page" in {
        val target = setupTarget(Some(PreviousTaxableGainsModel(1000)), otherProperties = true,
          allowableLossesModel = Some(AllowableLossesModel(true)), allowableLossesValueModel = Some(AllowableLossesValueModel(BigDecimal(1000))))
        val result = target.previousTaxableGains(fakeRequestWithSession)
        val doc = Jsoup.parse(bodyOf(result))

        val link = doc.select("#back-link")
        link.attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForwardValue().toString
      }
    }

    "no other shares AND brought forward losses specified" should {
      "return a back link to the brought forward input page" in {
        val target = setupTarget(Some(PreviousTaxableGainsModel(1000)), otherProperties = false, lossesBroughtForward = true,
          allowableLossesModel = None, allowableLossesValueModel = None)
        val result = target.previousTaxableGains(fakeRequestWithSession)
        val doc = Jsoup.parse(bodyOf(result))

        val link = doc.select("#back-link")
        link.attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForwardValue().toString
      }
    }

    "no other shares AND brought forward losses NOT specified" should {
      "return a back link to the brought forward choice page" in {
        val target = setupTarget(Some(PreviousTaxableGainsModel(1000)), otherProperties = false, lossesBroughtForward = false,
          allowableLossesModel = None, allowableLossesValueModel = None)
        val result = target.previousTaxableGains(fakeRequestWithSession)
        val doc = Jsoup.parse(bodyOf(result))

        val link = doc.select("#back-link")
        link.attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForward().toString
      }
    }
  }

  "request has an invalid session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.previousTaxableGains(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
    }
  }

  "Calling .submitPreviousTaxableGains from the IncomeController" when {

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitPreviousTaxableGains(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the previous taxable gains page" in {
        doc.title() shouldEqual messages.title("2015/16")
      }
    }

    "a valid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitPreviousTaxableGains(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.routes.IncomeController.currentIncome()}" in {
        redirectLocation(result).get shouldBe controllers.routes.IncomeController.currentIncome().toString
      }
    }
  }
}
