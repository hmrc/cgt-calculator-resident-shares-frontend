/*
 * Copyright 2020 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.Materializer
import assets.MessageLookup.{SharesAcquisitionCosts => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import config.ApplicationConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.{CgtLanguageController, GainController}
import controllers.helpers.FakeRequestHelper
import models.resident.AcquisitionCostsModel
import models.resident.shares.{GainAnswersModel, OwnerBeforeLegislationStartModel}
import models.resident.shares.gain.DidYouInheritThemModel
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AcquisitionCostsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {
  lazy val materializer = mock[Materializer]

  implicit lazy val actorSystem = ActorSystem()

  val gainAnswersModel = mock[GainAnswersModel]

  implicit val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val mockApplication = fakeApplication
  val mockCalcConnector = mock[CalculatorConnector]
  val mockSessionCacheConnector = mock[SessionCacheConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockMCC =fakeApplication.injector.instanceOf[MessagesControllerComponents]


  def setupTarget(
                   acquisitionCostsData: Option[AcquisitionCostsModel],
                   ownedBeforeStartOfTaxData: Option[OwnerBeforeLegislationStartModel],
                   inheritedThemData: Option[DidYouInheritThemModel],
                   gainAnswers: GainAnswersModel,
                   totalGain: BigDecimal
                 ): GainController = {

    when(mockSessionCacheConnector.fetchAndGetFormData[AcquisitionCostsModel]
      (ArgumentMatchers.eq(keystoreKeys.acquisitionCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(acquisitionCostsData))

    when(mockSessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel]
      (ArgumentMatchers.eq(keystoreKeys.ownerBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(ownedBeforeStartOfTaxData))

    when(mockSessionCacheConnector.fetchAndGetFormData[DidYouInheritThemModel]
      (ArgumentMatchers.eq(keystoreKeys.didYouInheritThem))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(inheritedThemData))

    when(mockSessionCacheService.getShareGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.calculateRttShareGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGain))

    when(mockSessionCacheConnector.saveFormData[AcquisitionCostsModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController(mockCalcConnector, mockSessionCacheService, mockSessionCacheConnector, mockMCC)
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
        Jsoup.parse(bodyOf(result)(materializer)).title shouldBe messages.title
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
        Jsoup.parse(bodyOf(result)(materializer)).title shouldBe messages.title
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

        s"have a back-link to '${controllers.routes.GainController.valueBeforeLegislationStart().url}'" in {
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

        s"have a back-link to '${controllers.routes.GainController.worthWhenInherited().url}'" in {
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

        s"have a back-link to '${controllers.routes.GainController.acquisitionValue().url}'" in {
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
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
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
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
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
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitAcquisitionCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DeductionsController.lossesBroughtForward().url)
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
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitAcquisitionCosts(request)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the acquisition costs page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }

}
