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

package forms

import common.Constants
import common.Transformers._
import common.Validation._
import models.resident.TaxYearModel
import models.resident.income.CurrentIncomeModel
import play.api.Logging
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.time.TaxYear

import javax.inject.Inject

class CurrentIncomeForm @Inject()() extends Logging {

  def apply(taxYear: String)(implicit messages: Messages): Form[CurrentIncomeModel] = {
    val question =  if(taxYear == TaxYearModel.convertWithWelsh(TaxYear.current.startYear.toString)) "questionCurrentYear" else "question"

    Form(
      mapping(
        "amount" -> text
          .transform(stripCurrencyCharacters, stripCurrencyCharacters)
          .verifying(messages(s"calc.resident.currentIncome.$question.error.mandatoryAmount", taxYear), mandatoryCheck)
          .verifying(messages(s"calc.resident.currentIncome.$question.error.invalidAmount", taxYear), bigDecimalCheck)
          .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
          .verifying(maxMonetaryValueConstraint(Constants.maxNumeric))
          .verifying(messages(s"calc.resident.currentIncome.$question.error.minimumAmount", taxYear), isPositive)
          .verifying(messages(s"calc.resident.currentIncome.$question.error.invalidAmount", taxYear), decimalPlacesCheck)
      )(CurrentIncomeModel.apply)(CurrentIncomeModel.unapply)
    )
  }

}
