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

package constructors

import common.Dates._
import models.resident.IncomeAnswersModel
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}

object CalculateRequestConstructor {

  def totalGainRequestString (answers: GainAnswersModel): String = {
    s"?disposalValue=${determineDisposalValueToUse(answers).toDouble}" +
      s"&disposalCosts=${answers.disposalCosts.toDouble}" +
      s"&acquisitionValue=${determineAcquisitionValueToUse(answers).toDouble}" +
      s"&acquisitionCosts=${answers.acquisitionCosts.toDouble}" +
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
      s"${if (answers.broughtForwardModel.get.option)
        s"&broughtForwardLosses=${answers.broughtForwardValueModel.get.amount.toDouble}"
      else ""}" +
      s"&annualExemptAmount=${maxAEA.toDouble}"
  }

  def incomeAnswersRequestString (deductionsAnswers: DeductionGainAnswersModel, answers: IncomeAnswersModel): String = {
      s"&previousIncome=${answers.currentIncomeModel.get.amount.toDouble}" +
      s"&personalAllowance=${answers.personalAllowanceModel.get.amount.toDouble}"
  }
}
