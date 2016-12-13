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

package connectors

import java.util.UUID

import common.KeystoreKeys
import models.resident
import models.resident.IncomeAnswersModel
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {

  val mockHttp = mock[HttpGet]
  val mockSessionCache = mock[SessionCache]
  val sessionId = UUID.randomUUID.toString

  object TargetCalculatorConnector extends CalculatorConnector {
    override val sessionCache = mockSessionCache
    override val http = mockHttp
    override val serviceUrl = "dummy"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  def mockResidentSharesFetchAndGetFormData(): Unit = {
    when(mockSessionCache.fetchAndGetEntry[resident.AcquisitionCostsModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.acquisitionCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionCostsModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AcquisitionValueModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.DisposalDateModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(resident.DisposalDateModel(1, 1, 2016))))

    when(mockSessionCache.fetchAndGetEntry[resident.DisposalCostsModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.disposalCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalCostsModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.SellForLessModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.sellForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.SellForLessModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.DisposalValueModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.disposalValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.WorthWhenSoldForLessModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.worthWhenSoldForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenSoldForLessModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.OtherPropertiesModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.OtherPropertiesModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AllowableLossesModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.allowableLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AllowableLossesModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AllowableLossesValueModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.allowableLossesValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AllowableLossesValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForward))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardValueModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForwardValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.income.CurrentIncomeModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.currentIncome))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.CurrentIncomeModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.income.PersonalAllowanceModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.personalAllowance))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.PersonalAllowanceModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.income.PreviousTaxableGainsModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.previousTaxableGains))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.PreviousTaxableGainsModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.shares.OwnerBeforeLegislationStartModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.ownerBeforeLegislationStart))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.OwnerBeforeLegislationStartModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.shares.gain.DidYouInheritThemModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.didYouInheritThem))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.gain.DidYouInheritThemModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.WorthWhenInheritedModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.worthWhenInherited))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenInheritedModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.shares.gain.ValueBeforeLegislationStartModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.valueBeforeLegislationStart))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.shares.gain.ValueBeforeLegislationStartModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AnnualExemptAmountModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.annualExemptAmount))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AnnualExemptAmountModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForward))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardValueModel](Matchers.eq(KeystoreKeys.ResidentShareKeys.lossesBroughtForwardValue))(Matchers.any(), Matchers.any()))
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
}
