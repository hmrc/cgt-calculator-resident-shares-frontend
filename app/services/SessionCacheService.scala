/*
 * Copyright 2023 HM Revenue & Customs
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
import models.resident
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.shares.gain.{DidYouInheritThemModel, ValueBeforeLegislationStartModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel, OwnerBeforeLegislationStartModel}
import play.api.libs.json.Format
import play.api.mvc.Request
import play.api.mvc.Results._
import repositories.SessionRepository
import uk.gov.hmrc.mongo.cache.DataKey
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionCacheService @Inject()(sessionRepository: SessionRepository)(implicit ec: ExecutionContext) {


  def saveFormData[T](key: String, data: T)(implicit request: Request[_], formats: Format[T]): Future[(String, String)] = {
    sessionRepository.putSession[T](DataKey(key), data)
  }

  def fetchAndGetFormData[T](key: String)(implicit request: Request[_], formats: Format[T]): Future[Option[T]] = {
    sessionRepository.getFromSession[T](DataKey(key))
  }

  def clearKeystore(implicit request: Request[_]): Future[Unit] = {
    sessionRepository.clear(request)
  }

  def getShareGainAnswers(implicit request: Request[_]): Future[GainAnswersModel] = {
    val disposalDate = fetchAndGetFormData[DisposalDateModel](ResidentShareKeys.disposalDate)
      .map(formData => constructDate(formData.get.day, formData.get.month, formData.get.year))

    val soldForLessThanWorth = fetchAndGetFormData[SellForLessModel](ResidentShareKeys.sellForLess)
      .map(_.get.sellForLess)

    val disposalValue = fetchAndGetFormData[DisposalValueModel](ResidentShareKeys.disposalValue)
      .map(_.map(_.amount))

    val worthWhenSoldForLess = fetchAndGetFormData[WorthWhenSoldForLessModel](ResidentShareKeys.worthWhenSoldForLess)
      .map(_.map(_.amount))

    val disposalCosts = fetchAndGetFormData[DisposalCostsModel](ResidentShareKeys.disposalCosts)
      .map(_.get.amount)

    val ownedBeforeTaxStartDate = fetchAndGetFormData[OwnerBeforeLegislationStartModel](ResidentShareKeys.ownerBeforeLegislationStart)
      .map(_.get.ownerBeforeLegislationStart)

    val valueBeforeLegislationStart = fetchAndGetFormData[ValueBeforeLegislationStartModel](ResidentShareKeys.valueBeforeLegislationStart)
      .map(_.map(_.amount))

    val inheritedTheShares = fetchAndGetFormData[DidYouInheritThemModel](ResidentShareKeys.didYouInheritThem)
      .map(_.map(_.wereInherited))

    val worthWhenInherited = fetchAndGetFormData[WorthWhenInheritedModel](ResidentShareKeys.worthWhenInherited)
      .map(_.map(_.amount))

    val acquisitionValue = fetchAndGetFormData[AcquisitionValueModel](ResidentShareKeys.acquisitionValue)
      .map(_.map(_.amount))

    val acquisitionCosts = fetchAndGetFormData[AcquisitionCostsModel](ResidentShareKeys.acquisitionCosts)
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
        Redirect(controllers.utils.routes.TimeoutController.timeout),
        e.getMessage
      )
  }

  def getShareDeductionAnswers(implicit request: Request[_]): Future[DeductionGainAnswersModel] = {
    val broughtForwardModel = fetchAndGetFormData[LossesBroughtForwardModel](ResidentShareKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[LossesBroughtForwardValueModel](ResidentShareKeys.lossesBroughtForwardValue)

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
        Redirect(controllers.utils.routes.TimeoutController.timeout),
        e.getMessage
      )
  }

  def getShareIncomeAnswers(implicit request: Request[_]): Future[IncomeAnswersModel] = {
    val currentIncomeModel = fetchAndGetFormData[CurrentIncomeModel](ResidentShareKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[PersonalAllowanceModel](ResidentShareKeys.personalAllowance)

    for {
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      resident.IncomeAnswersModel(currentIncome, personalAllowance)
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout),
        e.getMessage
      )
  }
}
