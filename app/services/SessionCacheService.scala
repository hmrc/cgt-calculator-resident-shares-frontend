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

package services

import common.Dates._
import common.KeystoreKeys.ResidentShareKeys
import connectors.SessionCacheConnector
import models.resident
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.{AcquisitionCostsModel, AcquisitionValueModel, WorthWhenInheritedModel, _}
import models.resident.shares.gain.{DidYouInheritThemModel, ValueBeforeLegislationStartModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel, OwnerBeforeLegislationStartModel}
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.exceptions.ApplicationException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SessionCacheService extends SessionCacheService {
  override val sessionCacheConnector: SessionCacheConnector = SessionCacheConnector
}

trait SessionCacheService {
  val sessionCacheConnector: SessionCacheConnector
  val homeLink = controllers.routes.GainController.disposalDate().url

  def getShareGainAnswers(implicit hc: HeaderCarrier): Future[GainAnswersModel] = {

    val disposalDate = sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](ResidentShareKeys.disposalDate)
      .map(formData => constructDate(formData.get.day, formData.get.month, formData.get.year))

    val soldForLessThanWorth = sessionCacheConnector.fetchAndGetFormData[SellForLessModel](ResidentShareKeys.sellForLess)
      .map(_.get.sellForLess)

    val disposalValue = sessionCacheConnector.fetchAndGetFormData[DisposalValueModel](ResidentShareKeys.disposalValue)
      .map(_.map(_.amount))

    val worthWhenSoldForLess = sessionCacheConnector.fetchAndGetFormData[WorthWhenSoldForLessModel](ResidentShareKeys.worthWhenSoldForLess)
      .map(_.map(_.amount))

    val disposalCosts = sessionCacheConnector.fetchAndGetFormData[DisposalCostsModel](ResidentShareKeys.disposalCosts)
      .map(_.get.amount)

    val ownedBeforeTaxStartDate = sessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](ResidentShareKeys.ownerBeforeLegislationStart)
      .map(_.get.ownerBeforeLegislationStart)

    val valueBeforeLegislationStart = sessionCacheConnector.fetchAndGetFormData[ValueBeforeLegislationStartModel](ResidentShareKeys.valueBeforeLegislationStart)
      .map(_.map(_.amount))

    val inheritedTheShares = sessionCacheConnector.fetchAndGetFormData[DidYouInheritThemModel](ResidentShareKeys.didYouInheritThem)
      .map(_.map(_.wereInherited))

    val worthWhenInherited = sessionCacheConnector.fetchAndGetFormData[WorthWhenInheritedModel](ResidentShareKeys.worthWhenInherited)
      .map(_.map(_.amount))

    val acquisitionValue = sessionCacheConnector.fetchAndGetFormData[AcquisitionValueModel](ResidentShareKeys.acquisitionValue)
      .map(_.map(_.amount))

    val acquisitionCosts = sessionCacheConnector.fetchAndGetFormData[AcquisitionCostsModel](ResidentShareKeys.acquisitionCosts)
      .map(_.get.amount)

    for {
      disposalDate <- disposalDate
      soldForLessThanWorth <- soldForLessThanWorth
      disposalValue <- disposalValue
      worthWhenSoldForLess <- worthWhenSoldForLess
      disposalCosts <- disposalCosts
      ownedBeforeTaxStartDate <- ownedBeforeTaxStartDate
      valueBeforeLegislationStart <- valueBeforeLegislationStart
      inheritedTheShares <- inheritedTheShares
      worthWhenInherited <- worthWhenInherited
      acquisitionValue <- acquisitionValue
      acquisitionCosts <- acquisitionCosts
    } yield GainAnswersModel(
      disposalDate,
      soldForLessThanWorth,
      disposalValue,
      worthWhenSoldForLess,
      disposalCosts,
      ownedBeforeTaxStartDate,
      valueBeforeLegislationStart,
      inheritedTheShares,
      worthWhenInherited,
      acquisitionValue,
      acquisitionCosts
    )
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calc-resident-shares-fe",
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  def getShareDeductionAnswers(implicit hc: HeaderCarrier): Future[DeductionGainAnswersModel] = {
    val broughtForwardModel = sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardModel](ResidentShareKeys.lossesBroughtForward)
    val broughtForwardValueModel = sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](ResidentShareKeys.lossesBroughtForwardValue)

    for {
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
    } yield {
      resident.shares.DeductionGainAnswersModel(
        broughtForward,
        broughtForwardValue)
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calc-resident-shares-fe",
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  def getShareIncomeAnswers(implicit hc: HeaderCarrier): Future[IncomeAnswersModel] = {
    val currentIncomeModel = sessionCacheConnector.fetchAndGetFormData[CurrentIncomeModel](ResidentShareKeys.currentIncome)
    val personalAllowanceModel = sessionCacheConnector.fetchAndGetFormData[PersonalAllowanceModel](ResidentShareKeys.personalAllowance)

    for {
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      resident.IncomeAnswersModel(currentIncome, personalAllowance)
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calc-resident-shares-fe",
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }
}
