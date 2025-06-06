/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.GainControllerSpec

import assets.MessageLookup.{SharesAcquisitionCosts => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.GainController
import controllers.helpers.FakeRequestHelper
import models.resident.AcquisitionCostsModel
import models.resident.shares.gain.DidYouInheritThemModel
import models.resident.shares.{GainAnswersModel, OwnerBeforeLegislationStartModel}
import org.apache.pekko.actor.ActorSystem
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import views.html.calculation.gain._
import views.html.calculation.outsideTaxYear

import scala.concurrent.Future

class AcquisitionCostsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  val gainAnswersModel: GainAnswersModel = mock[GainAnswersModel]

  implicit val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val mockApplication: Application = fakeApplication
  val mockCalcConnector: CalculatorConnector = mock[CalculatorConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockMCC: MessagesControllerComponents =fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val acquisitionCostsView: acquisitionCosts = fakeApplication.injector.instanceOf[acquisitionCosts]
  val acquisitionValueView: acquisitionValue = fakeApplication.injector.instanceOf[acquisitionValue]
  val disposalCostsView: disposalCosts = fakeApplication.injector.instanceOf[disposalCosts]
  val disposalDateView: disposalDate = fakeApplication.injector.instanceOf[disposalDate]
  val disposalValueView: disposalValue = fakeApplication.injector.instanceOf[disposalValue]
  val didYouInheritThemView: didYouInheritThem = fakeApplication.injector.instanceOf[didYouInheritThem]
  val ownerBeforeLegislationStartView: ownerBeforeLegislationStart = fakeApplication.injector.instanceOf[ownerBeforeLegislationStart]
  val sellForLessView: sellForLess = fakeApplication.injector.instanceOf[sellForLess]
  val valueBeforeLegislationStartView: valueBeforeLegislationStart = fakeApplication.injector.instanceOf[valueBeforeLegislationStart]
  val worthWhenInheritedView: worthWhenInherited = fakeApplication.injector.instanceOf[worthWhenInherited]
  val worthWhenSoldForLessView: worthWhenSoldForLess = fakeApplication.injector.instanceOf[worthWhenSoldForLess]
  val outsideTaxYearView: outsideTaxYear = fakeApplication.injector.instanceOf[outsideTaxYear]

  def setupTarget(
                   acquisitionCostsData: Option[AcquisitionCostsModel],
                   ownedBeforeStartOfTaxData: Option[OwnerBeforeLegislationStartModel],
                   inheritedThemData: Option[DidYouInheritThemModel],
                   gainAnswers: GainAnswersModel,
                   totalGain: BigDecimal
                 ): GainController = {

    when(mockSessionCacheService.fetchAndGetFormData[AcquisitionCostsModel]
      (ArgumentMatchers.eq(keystoreKeys.acquisitionCosts))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionCostsData))

    when(mockSessionCacheService.fetchAndGetFormData[OwnerBeforeLegislationStartModel]
      (ArgumentMatchers.eq(keystoreKeys.ownerBeforeLegislationStart))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(ownedBeforeStartOfTaxData))

    when(mockSessionCacheService.fetchAndGetFormData[DidYouInheritThemModel]
      (ArgumentMatchers.eq(keystoreKeys.didYouInheritThem))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(inheritedThemData))

    when(mockSessionCacheService.getShareGainAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.calculateRttShareGrossGain(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGain))

    when(mockSessionCacheService.saveFormData[AcquisitionCostsModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    new GainController(mockCalcConnector, mockSessionCacheService, mockMCC,
      acquisitionCostsView, acquisitionValueView, disposalCostsView, disposalDateView, disposalValueView,
      didYouInheritThemView, ownerBeforeLegislationStartView, sellForLessView, valueBeforeLegislationStartView,
      worthWhenInheritedView, worthWhenSoldForLessView, outsideTaxYearView)
  }

  "Calling .acquisitionCosts from the shares GainCalculationController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(
        acquisitionCostsData = None,
        ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
        inheritedThemData = Some(DidYouInheritThemModel(false)),
        gainAnswersModel,
        BigDecimal(0)
      )
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Acquisition Costs view" in {
        Jsoup.parse(contentAsString(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(
        acquisitionCostsData = Some(AcquisitionCostsModel(1000)),
        ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
        inheritedThemData = Some(DidYouInheritThemModel(false)),
        gainAnswersModel,
        BigDecimal(0)
      )
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Acquisition Costs view" in {
        Jsoup.parse(contentAsString(result)).title shouldBe messages.title
      }
    }

    "testing the back links" when {

      "the property was owned before the tax came in (1 April 1982)" should {

        lazy val target = setupTarget(
          acquisitionCostsData = None,
          ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(true)),
          inheritedThemData = None,
          gainAnswersModel,
          BigDecimal(0)
        )
        lazy val result = target.acquisitionCosts(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        s"have a back-link to '${controllers.routes.GainController.valueBeforeLegislationStart.url}'" in {
          status(result) shouldBe 200
        }
      }

      "the property was acquired after the tax came in (1 April 1982) and it was inherited" should {

        lazy val target = setupTarget(
          acquisitionCostsData = None,
          ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
          inheritedThemData = Some(DidYouInheritThemModel(true)),
          gainAnswersModel,
          BigDecimal(0)
        )
        lazy val result = target.acquisitionCosts(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        s"have a back-link to '${controllers.routes.GainController.worthWhenInherited.url}'" in {
          status(result) shouldBe 200
        }
      }

      "the property was acquired after the tax came in (1 April 1982) and it was not inherited" should {

        lazy val target = setupTarget(
          acquisitionCostsData = None,
          ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
          inheritedThemData = Some(DidYouInheritThemModel(false)),
          gainAnswersModel,
          BigDecimal(0)
        )
        lazy val result = target.acquisitionCosts(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        s"have a back-link to '${controllers.routes.GainController.acquisitionValue.url}'" in {
          status(result) shouldBe 200
        }
      }
    }
  }

  "request has an invalid session" should {

    lazy val target = setupTarget(
      acquisitionCostsData = None,
      ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
      inheritedThemData = Some(DidYouInheritThemModel(false)),
      gainAnswersModel,
      BigDecimal(0)
    )
    lazy val result = target.acquisitionCosts(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
    }
  }

  "Calling .submitAcquisitionCosts from the shares GainCalculationController" when {

    "a valid form is submitted that results in a zero gain" should {
      lazy val target = setupTarget(
        acquisitionCostsData = None,
        ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
        inheritedThemData = Some(DidYouInheritThemModel(false)),
        gainAnswersModel,
        BigDecimal(0)
      )
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000")).withMethod("POST")
      lazy val result = target.submitAcquisitionCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/review-your-answers-gain")
      }
    }

    "a valid form is submitted that results in a loss" should {
      lazy val target = setupTarget(
        acquisitionCostsData = None,
        ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
        inheritedThemData = Some(DidYouInheritThemModel(false)),
        gainAnswersModel,
        BigDecimal(-1500)
      )
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000")).withMethod("POST")
      lazy val result = target.submitAcquisitionCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/review-your-answers-gain")
      }
    }

    "a valid form is submitted that results in a gain" should {
      lazy val target = setupTarget(
        acquisitionCostsData = None,
        ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
        inheritedThemData = Some(DidYouInheritThemModel(false)),
        gainAnswersModel,
        BigDecimal(1000)
      )
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000")).withMethod("POST")
      lazy val result = target.submitAcquisitionCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DeductionsController.lossesBroughtForward.url)
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(
        acquisitionCostsData = None,
        ownedBeforeStartOfTaxData = Some(OwnerBeforeLegislationStartModel(false)),
        inheritedThemData = Some(DidYouInheritThemModel(false)),
        gainAnswersModel,
        BigDecimal(0)
      )
      lazy val request = fakeRequestToPOSTWithSession(("amount", "")).withMethod("POST")
      lazy val result = target.submitAcquisitionCosts(request)
      lazy val doc = Jsoup.parse(contentAsString(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the acquisition costs page" in {
        doc.title() shouldEqual s"Error: ${messages.title}"
      }
    }
  }

}
