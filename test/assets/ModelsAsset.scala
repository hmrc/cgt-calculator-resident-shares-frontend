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

package assets

import common.Dates
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.shares._

object ModelsAsset {

  val gainAnswersMostPossibles = GainAnswersModel(Dates.constructDate(10, 10, 2016),
    soldForLessThanWorth = false,
    Some(200000),
    None,
    10000,
    ownerBeforeLegislationStart = false,
    None,
    inheritedTheShares = Some(false),
    None,
    Some(100000),
    10000
  )

  val gainAnswersWithException = GainAnswersModel(Dates.constructDate(10, 10, 2016),
    soldForLessThanWorth = true,
    Some(200000),
    None,
    10000,
    ownerBeforeLegislationStart = false,
    None,
    inheritedTheShares = Some(false),
    None,
    Some(100000),
    10000
  )

  val gainAnswersLeastPossibles = GainAnswersModel(Dates.constructDate(10, 10, 2016),
    soldForLessThanWorth = true,
    None,
    Some(3000),
    200,
    ownerBeforeLegislationStart = true,
    Some(5000),
    None,
    None,
    None,
    10000)

  val gainLargeDisposalValue = GainAnswersModel(Dates.constructDate(10, 10, 2016),
    soldForLessThanWorth = false,
    disposalValue = Some(100000),
    worthWhenSoldForLess = None,
    disposalCosts = BigDecimal(1000000),
    ownerBeforeLegislationStart = false,
    valueBeforeLegislationStart = None,
    inheritedTheShares = Some(false),
    worthWhenInherited = None,
    acquisitionValue = None,
    acquisitionCosts = 30000
  )

  val gainLowDisposalValue = GainAnswersModel(Dates.constructDate(10, 10, 2016),
    soldForLessThanWorth = false,
    disposalValue = Some(1000),
    worthWhenSoldForLess = None,
    disposalCosts = BigDecimal(1000000),
    ownerBeforeLegislationStart = false,
    valueBeforeLegislationStart = None,
    inheritedTheShares = Some(false),
    worthWhenInherited = None,
    acquisitionValue = None,
    acquisitionCosts = 30000
  )

  val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(
    gain = 50000,
    chargeableGain = 20000,
    aeaUsed = 10,
    deductions = 30000,
    taxOwed = 3600,
    firstBand = 20000,
    firstRate = 18,
    secondBand = Some(10000.00),
    secondRate = Some(28),
    lettingReliefsUsed = Some(BigDecimal(500)),
    prrUsed = Some(BigDecimal(125)),
    broughtForwardLossesUsed = 35,
    allowableLossesUsed = 0,
    baseRateTotal = 30000,
    upperRateTotal = 15000
  )

  val deductionAnswersMostPossibles = DeductionGainAnswersModel(
    Some(LossesBroughtForwardModel(true)),
    Some(LossesBroughtForwardValueModel(10000))
  )

  val deductionAnswersLeastPossibles = DeductionGainAnswersModel(
    Some(LossesBroughtForwardModel(false)),
    None
  )

  val incomeAnswers = IncomeAnswersModel(Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

  val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

}
