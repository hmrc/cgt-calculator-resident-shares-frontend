/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors

import java.time.LocalDate
import java.util.UUID

import common.KeystoreKeys
import models.resident
import models.resident.IncomeAnswersModel
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.http.logging.SessionId

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {

  val mockHttp = mock[HttpGet]
  val mockSessionCacheConnector = mock[SessionCacheConnector]
  val sessionId = UUID.randomUUID.toString

  object TargetCalculatorConnector extends CalculatorConnector {
    override val sessionCacheConnector = mockSessionCacheConnector
    override val http = mockHttp
    override val serviceUrl = "dummy"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  def mockResidentSharesFetchAndGetFormData(): Unit = {
    when(mockSessionCacheConnector.fetchAndGetFormData[resident.AcquisitionCostsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.acquisitionCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionCostsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.AcquisitionValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.acquisitionValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalDateModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(resident.DisposalDateModel(1, 1, 2016))))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalCostsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.disposalCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalCostsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.SellForLessModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.sellForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.SellForLessModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.disposalValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.WorthWhenSoldForLessModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.worthWhenSoldForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenSoldForLessModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForward))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForwardValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.CurrentIncomeModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.currentIncome))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.CurrentIncomeModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.PersonalAllowanceModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.personalAllowance))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.PersonalAllowanceModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.PreviousTaxableGainsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.previousTaxableGains))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.PreviousTaxableGainsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.shares.OwnerBeforeLegislationStartModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.ownerBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.OwnerBeforeLegislationStartModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.shares.gain.DidYouInheritThemModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.didYouInheritThem))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.gain.DidYouInheritThemModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.WorthWhenInheritedModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.worthWhenInherited))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenInheritedModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.shares.gain.ValueBeforeLegislationStartModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.valueBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.gain.ValueBeforeLegislationStartModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForward))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForwardValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))
  }

  "Calling getShareGainAnswers" should {

    "return a valid ShareGainAnswersModel" in {
      val hc = mock[HeaderCarrier]
      mockResidentSharesFetchAndGetFormData()
      lazy val result = TargetCalculatorConnector.getShareGainAnswers(hc)
      await(result).isInstanceOf[GainAnswersModel] shouldBe true
    }
  }

  "Calling getShareDeductionAnswers" should {

    "return a valid DeductionGainAnswersModel" in {
      val hc = mock[HeaderCarrier]
      mockResidentSharesFetchAndGetFormData()
      lazy val result = TargetCalculatorConnector.getShareDeductionAnswers(hc)
      await(result).isInstanceOf[DeductionGainAnswersModel] shouldBe true
    }
  }

  "Calling getShareIncomeAnswers" should {

    "return a valid DeductionGainAnswersModel" in {
      val hc = mock[HeaderCarrier]
      mockResidentSharesFetchAndGetFormData()
      lazy val result = TargetCalculatorConnector.getShareIncomeAnswers(hc)
      await(result).isInstanceOf[IncomeAnswersModel] shouldBe true
    }
  }


    "Calling .getMinimumDate" should {
      def mockDate(result: Future[DateTime]): OngoingStubbing[Future[DateTime]] =
          when(mockHttp.GET[DateTime](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(result)

        "return a DateTime which matches the returned LocalDate" in {
          mockDate(Future.successful(DateTime.parse("2015-06-04")))
          await(TargetCalculatorConnector.getMinimumDate()) shouldBe LocalDate.parse("2015-06-04")
        }

        "return a failure if one occurs" in {
          mockDate(Future.failed(new Exception("error message")))
          the[Exception] thrownBy await(TargetCalculatorConnector.getMinimumDate()) should have message "error message"
        }
    }
}
