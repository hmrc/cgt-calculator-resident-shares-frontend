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

package constructors.resident.shares

import common.Dates._
import models.resident.{AllowableLossesModel, AllowableLossesValueModel, IncomeAnswersModel, OtherPropertiesModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}

object CalculateRequestConstructor {

  def totalGainRequestString (answers: GainAnswersModel): String = {
    s"?disposalValue=${determineDisposalValueToUse(answers)}" +
      s"&disposalCosts=${answers.disposalCosts}" +
      s"&acquisitionValue=${determineAcquisitionValueToUse(answers)}" +
      s"&acquisitionCosts=${answers.acquisitionCosts}" +
      s"&disposalDate=${answers.disposalDate.format(requestFormatter)}"
  }

  def determineDisposalValueToUse (answers: GainAnswersModel): BigDecimal = {
    if (answers.soldForLessThanWorth) answers.worthWhenSoldForLess.get
    else answers.disposalValue.get
  }

  def determineAcquisitionValueToUse (answers: GainAnswersModel): BigDecimal = {
    if (answers.ownerBeforeLegislationStart) answers.valueBeforeLegislationStart.get
    else if (answers.inheritedTheShares.get) answers.worthWhenInherited.get
    else answers.acquisitionValue.get
  }

  def chargeableGainRequestString (answers: DeductionGainAnswersModel, maxAEA: BigDecimal): String = {
      s"${if (answers.otherPropertiesModel.get.hasOtherProperties && answers.allowableLossesModel.get.isClaiming)
        s"&allowableLosses=${answers.allowableLossesValueModel.get.amount}"
      else ""}" +
      s"${if (answers.broughtForwardModel.get.option)
        s"&broughtForwardLosses=${answers.broughtForwardValueModel.get.amount}"
      else ""}" +
      s"&annualExemptAmount=${if (isUsingAnnualExemptAmount(answers.otherPropertiesModel, answers.allowableLossesModel, answers.allowableLossesValueModel)) {
        answers.annualExemptAmountModel.get.amount}
      else maxAEA}"
  }

  def isUsingAnnualExemptAmount (otherPropertiesModel: Option[OtherPropertiesModel],
                                 allowableLossesModel: Option[AllowableLossesModel],
                                 allowableLossesValueModel: Option[AllowableLossesValueModel]): Boolean = {
    (otherPropertiesModel, allowableLossesModel, allowableLossesValueModel) match {
      case (Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(true)), Some(AllowableLossesValueModel(data)))
        if data == 0 => true
      case (Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(false)), _) => true
      case _ => false
    }
  }

  def incomeAnswersRequestString (deductionsAnswers: DeductionGainAnswersModel, answers: IncomeAnswersModel): String = {
    s"${if (deductionsAnswers.otherPropertiesModel.get.hasOtherProperties && !deductionsAnswers.allowableLossesModel.get.isClaiming &&
      deductionsAnswers.annualExemptAmountModel.get.amount == 0)
      s"&previousTaxableGain=${answers.previousTaxableGainsModel.get.amount}"
    else ""}" +
      s"&previousIncome=${answers.currentIncomeModel.get.amount}" +
      s"&personalAllowance=${answers.personalAllowanceModel.get.amount}"
  }
}
