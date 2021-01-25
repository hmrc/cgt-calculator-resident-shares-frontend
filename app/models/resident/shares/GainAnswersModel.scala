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

package models.resident.shares

import java.time.LocalDate

case class GainAnswersModel(disposalDate: LocalDate,
                            soldForLessThanWorth: Boolean,
                            disposalValue: Option[BigDecimal],
                            worthWhenSoldForLess: Option[BigDecimal],
                            disposalCosts: BigDecimal,
                            ownerBeforeLegislationStart: Boolean,
                            valueBeforeLegislationStart: Option[BigDecimal],
                            inheritedTheShares: Option[Boolean],
                            worthWhenInherited: Option[BigDecimal],
                            acquisitionValue: Option[BigDecimal],
                            acquisitionCosts: BigDecimal){

  val displayWorthWhenBought: Boolean = !ownerBeforeLegislationStart && !inheritedTheShares.get
  val displayWorthWhenInherited: Boolean = !ownerBeforeLegislationStart && inheritedTheShares.get

}
