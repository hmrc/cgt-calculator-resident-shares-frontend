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

package constructors

import common.Dates._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import models.resident.IncomeAnswersModel

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
      s"${if (answers.broughtForwardModel.get.option)
        s"&broughtForwardLosses=${answers.broughtForwardValueModel.get.amount}"
      else ""}" +
      s"&annualExemptAmount=$maxAEA"
  }

  def incomeAnswersRequestString (deductionsAnswers: DeductionGainAnswersModel, answers: IncomeAnswersModel): String = {
      s"&previousIncome=${answers.currentIncomeModel.get.amount}" +
      s"&personalAllowance=${answers.personalAllowanceModel.get.amount}"
  }
}
