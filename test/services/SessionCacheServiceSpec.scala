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

package services

import common.KeystoreKeys.{ResidentShareKeys => Keys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import models.resident
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results._
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.CurrentTimestampSupport
import uk.gov.hmrc.mongo.test.MongoSupport
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import java.util.UUID
import scala.concurrent.Future

class SessionCacheServiceSpec extends CommonPlaySpec with MockitoSugar with WithCommonFakeApplication with MongoSupport {

  val sessionRepository = new SessionRepository(mongoComponent = mongoComponent,
    config = fakeApplication.configuration, timestampSupport = new CurrentTimestampSupport())
  val sessionId: String = UUID.randomUUID.toString
  val sessionPair: (String, String) = SessionKeys.sessionId -> sessionId
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionPair)

  val sessionCacheService: SessionCacheService = new SessionCacheService(sessionRepository)

  val acquisitionCosts: AcquisitionCostsModel = resident.AcquisitionCostsModel(100)
  val acquisitionValue: AcquisitionValueModel = resident.AcquisitionValueModel(200)
  val disposalDate: DisposalDateModel = resident.DisposalDateModel(1, 1, 2016)
  val disposalCosts: DisposalCostsModel = resident.DisposalCostsModel(300)
  val sellForLess: SellForLessModel = resident.SellForLessModel(true)
  val disposalValue: resident.DisposalValueModel = resident.DisposalValueModel(400)
  val worthWhenSoldForLess: resident.WorthWhenSoldForLessModel = resident.WorthWhenSoldForLessModel(500)
  val lossesBroughtForward: resident.LossesBroughtForwardModel = resident.LossesBroughtForwardModel(false)
  val lossesBroughtForwardValue: resident.LossesBroughtForwardValueModel = resident.LossesBroughtForwardValueModel(600)
  val currentIncome: resident.income.CurrentIncomeModel = resident.income.CurrentIncomeModel(700)
  val personalAllowance: resident.income.PersonalAllowanceModel = resident.income.PersonalAllowanceModel(800)
  val previousTaxableGains: resident.income.PreviousTaxableGainsModel = resident.income.PreviousTaxableGainsModel(900)
  val ownerBeforeLegislationStart: resident.shares.OwnerBeforeLegislationStartModel = resident.shares.OwnerBeforeLegislationStartModel(true)
  val didYouInheritThem: resident.shares.gain.DidYouInheritThemModel = resident.shares.gain.DidYouInheritThemModel(false)
  val worthWhenInherited: resident.WorthWhenInheritedModel = resident.WorthWhenInheritedModel(1000)
  val valueBeforeLegislationStart: resident.shares.gain.ValueBeforeLegislationStartModel = resident.shares.gain.ValueBeforeLegislationStartModel(1100)

  class Setup(initializeCache: Boolean = true) {
    await {
      if (initializeCache) {
        sessionCacheService.saveFormData(Keys.acquisitionCosts, acquisitionCosts)
        sessionCacheService.saveFormData(Keys.acquisitionValue, acquisitionValue)
        sessionCacheService.saveFormData(Keys.disposalDate, disposalDate)
        sessionCacheService.saveFormData(Keys.disposalCosts, disposalCosts)
        sessionCacheService.saveFormData(Keys.sellForLess, sellForLess)
        sessionCacheService.saveFormData(Keys.disposalValue, disposalValue)
        sessionCacheService.saveFormData(Keys.worthWhenSoldForLess, worthWhenSoldForLess)
        sessionCacheService.saveFormData(Keys.lossesBroughtForward, lossesBroughtForward)
        sessionCacheService.saveFormData(Keys.lossesBroughtForwardValue, lossesBroughtForwardValue)
        sessionCacheService.saveFormData(Keys.currentIncome, currentIncome)
        sessionCacheService.saveFormData(Keys.personalAllowance, personalAllowance)
        sessionCacheService.saveFormData(Keys.previousTaxableGains, previousTaxableGains)
        sessionCacheService.saveFormData(Keys.ownerBeforeLegislationStart, ownerBeforeLegislationStart)
        sessionCacheService.saveFormData(Keys.didYouInheritThem, didYouInheritThem)
        sessionCacheService.saveFormData(Keys.worthWhenInherited, worthWhenInherited)
        sessionCacheService.saveFormData(Keys.valueBeforeLegislationStart, valueBeforeLegislationStart)
        sessionCacheService.saveFormData(Keys.lossesBroughtForward, lossesBroughtForward)
        sessionCacheService.saveFormData(Keys.lossesBroughtForwardValue, lossesBroughtForwardValue)
      } else {
        sessionRepository.clear
      }
    }
  }

  "Calling getShareGainAnswers" should {
    "return a valid ShareGainAnswersModel" in new Setup {
      lazy val result: Future[GainAnswersModel] = sessionCacheService.getShareGainAnswers
      await(result).isInstanceOf[GainAnswersModel] shouldBe true
    }

    "return an exception when missing data" in new Setup(initializeCache = false) {
      lazy val result: Future[GainAnswersModel] = sessionCacheService.getShareGainAnswers

      the[ApplicationException] thrownBy await(result) shouldBe ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout()), "None.get")
    }
  }

  "Calling getShareDeductionAnswers" should {
    "return a valid DeductionGainAnswersModel" in new Setup {
      lazy val result: Future[DeductionGainAnswersModel] = sessionCacheService.getShareDeductionAnswers
      await(result).isInstanceOf[DeductionGainAnswersModel] shouldBe true
    }

  }

  "Calling getShareIncomeAnswers" should {
    "return a valid DeductionGainAnswersModel" in new Setup {
      lazy val result: Future[IncomeAnswersModel] = sessionCacheService.getShareIncomeAnswers
      await(result).isInstanceOf[IncomeAnswersModel] shouldBe true
    }

  }
}
