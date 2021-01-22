/*
 * Copyright 2021 HM Revenue & Customs
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

import common.{CommonPlaySpec, KeystoreKeys}
import connectors.SessionCacheConnector
import models.resident
import models.resident.IncomeAnswersModel
import models.resident.income.CurrentIncomeModel
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import scala.concurrent.Future

class SessionCacheServiceSpec extends CommonPlaySpec with MockitoSugar {

  val mockSessionCacheConnector = mock[SessionCacheConnector]
  val homeLink = controllers.routes.GainController.disposalDate().url
  val mockSessionCacheService = mock[SessionCacheService]

  object TestSessionCacheService extends SessionCacheService(mockSessionCacheConnector) {
  }


  def mockResidentSharesFetchAndGetFormData(): Unit = {
    when(mockSessionCacheConnector.fetchAndGetFormData[resident.AcquisitionCostsModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.acquisitionCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionCostsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.AcquisitionValueModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.acquisitionValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalDateModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(resident.DisposalDateModel(1, 1, 2016))))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalCostsModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.disposalCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalCostsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.SellForLessModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.sellForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.SellForLessModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalValueModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.disposalValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.WorthWhenSoldForLessModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.worthWhenSoldForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenSoldForLessModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForward))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardValueModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForwardValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.CurrentIncomeModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.currentIncome))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.CurrentIncomeModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.PersonalAllowanceModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.personalAllowance))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.PersonalAllowanceModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.PreviousTaxableGainsModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.previousTaxableGains))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.PreviousTaxableGainsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.shares.OwnerBeforeLegislationStartModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.ownerBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.OwnerBeforeLegislationStartModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.shares.gain.DidYouInheritThemModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.didYouInheritThem))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.gain.DidYouInheritThemModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.WorthWhenInheritedModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.worthWhenInherited))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenInheritedModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.shares.gain.ValueBeforeLegislationStartModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.valueBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.gain.ValueBeforeLegislationStartModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForward))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardValueModel]
      (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForwardValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))
  }

  "Calling getShareGainAnswers" should {

    "return a valid ShareGainAnswersModel" in {
      val hc = mock[HeaderCarrier]
      mockResidentSharesFetchAndGetFormData()
      lazy val result = TestSessionCacheService.getShareGainAnswers(hc)
      await(result).isInstanceOf[GainAnswersModel] shouldBe true
    }

    "return an exception when missing data" in {
      mockResidentSharesFetchAndGetFormData()
      when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalDateModel]
        (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))
      val hc = mock[HeaderCarrier]
      lazy val result = TestSessionCacheService.getShareGainAnswers(hc)

      the[ApplicationException] thrownBy await(result) shouldBe ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }

  "Calling getShareDeductionAnswers" should {

    "return a valid DeductionGainAnswersModel" in {
      val hc = mock[HeaderCarrier]
      mockResidentSharesFetchAndGetFormData()
      lazy val result = TestSessionCacheService.getShareDeductionAnswers(hc)
      await(result).isInstanceOf[DeductionGainAnswersModel] shouldBe true
    }

    "return an exception when missing data" in {
      mockResidentSharesFetchAndGetFormData()
      when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardModel]
        (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForward))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))
      val hc = mock[HeaderCarrier]
      lazy val result = TestSessionCacheService.getShareDeductionAnswers(hc)

      the[ApplicationException] thrownBy await(result) shouldBe ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }

  "Calling getShareIncomeAnswers" should {

    "return a valid DeductionGainAnswersModel" in {
      val hc = mock[HeaderCarrier]
      mockResidentSharesFetchAndGetFormData()
      lazy val result = TestSessionCacheService.getShareIncomeAnswers(hc)
      await(result).isInstanceOf[IncomeAnswersModel] shouldBe true
    }

    "return an exception when missing data" in {
      mockResidentSharesFetchAndGetFormData()
      when(mockSessionCacheConnector.fetchAndGetFormData[CurrentIncomeModel]
        (ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.currentIncome))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))
      val hc = mock[HeaderCarrier]
      lazy val result = TestSessionCacheService.getShareIncomeAnswers(hc)

      the[ApplicationException] thrownBy await(result) shouldBe ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }
}
