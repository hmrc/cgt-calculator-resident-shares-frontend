/*
 * Copyright 2019 HM Revenue & Customs
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

import models.resident._
import models.resident.shares.DeductionGainAnswersModel
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.math.BigDecimal.RoundingMode

object SummaryConstructor {

  def gainMessage (result: BigDecimal): String = {
    if (result >= 0) Messages("calc.resident.summary.totalGain")
    else Messages("calc.resident.summary.totalLoss")
  }

  def broughtForwardLossesUsed (input: DeductionGainAnswersModel): String = {
    input.broughtForwardModel match {
      case Some(LossesBroughtForwardModel(true)) => MoneyPounds(input.broughtForwardValueModel.get.amount.setScale(0, RoundingMode.UP), 0).quantity
      case _ => MoneyPounds(0,0).quantity
    }
  }
}
