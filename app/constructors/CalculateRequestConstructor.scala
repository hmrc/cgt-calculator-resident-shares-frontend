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

  def totalGainRequest(answers: GainAnswersModel): Map[String, String] = {
    Map(
      "disposalValue" -> determineDisposalValueToUse(answers).toDouble.toString,
      "disposalCosts" -> answers.disposalCosts.toDouble.toString,
      "acquisitionValue" -> determineAcquisitionValueToUse(answers).toDouble.toString,
      "acquisitionCosts" -> answers.acquisitionCosts.toDouble.toString,
      "disposalDate" -> answers.disposalDate.format(requestFormatter)
    )
  }

  def determineDisposalValueToUse(answers: GainAnswersModel): BigDecimal = {
    if (answers.soldForLessThanWorth) answers.worthWhenSoldForLess.get
    else answers.disposalValue.get
  }

  def determineAcquisitionValueToUse(answers: GainAnswersModel): BigDecimal = {
    if (answers.ownerBeforeLegislationStart) answers.valueBeforeLegislationStart.get
    else if (answers.inheritedTheShares.get) answers.worthWhenInherited.get
    else answers.acquisitionValue.get
  }

  def chargeableGainRequest(answers: DeductionGainAnswersModel, maxAEA: BigDecimal): Map[String, String] = {
    Map("annualExemptAmount" -> maxAEA.toDouble.toString) ++ (
      if (answers.broughtForwardModel.get.option) {
        Seq("broughtForwardLosses" -> answers.broughtForwardValueModel.get.amount.toDouble.toString)
      } else Nil
      )
  }

  def incomeAnswersRequest(answers: IncomeAnswersModel): Map[String, Any] = {
    Map(
      "previousIncome" -> answers.currentIncomeModel.get.amount.toDouble,
      "personalAllowance" -> answers.personalAllowanceModel.get.amount.toDouble
    )
  }
}
