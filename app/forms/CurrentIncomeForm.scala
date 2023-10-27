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

package forms

import common.Constants
import common.Transformers._
import common.Validation._
import models.resident.TaxYearModel

import javax.inject.Inject
import models.resident.income.CurrentIncomeModel
import play.api.Logging
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{Lang, MessagesApi}
import uk.gov.hmrc.time.TaxYear

class CurrentIncomeForm @Inject()(implicit val messagesApi: MessagesApi) extends Logging {

  def apply(taxYear: String, lang: Lang): Form[CurrentIncomeModel] = {
    val question =  if(taxYear == TaxYearModel.convertWithWelsh(TaxYear.current.startYear.toString)(lang)) "questionCurrentYear" else "question"

    Form(
      mapping(
        "amount" -> text
          .verifying(messagesApi(s"calc.resident.currentIncome.$question.error.mandatoryAmount", taxYear)(lang), mandatoryCheck)
          .verifying(messagesApi(s"calc.resident.currentIncome.$question.error.invalidAmount", taxYear)(lang), bigDecimalCheck)
          .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
          .verifying(maxMonetaryValueConstraint(Constants.maxNumeric))
          .verifying(messagesApi(s"calc.resident.currentIncome.$question.error.minimumAmount", taxYear)(lang), isPositive)
          .verifying(messagesApi(s"calc.resident.currentIncome.$question.error.invalidAmount", taxYear)(lang), decimalPlacesCheck)
      )(CurrentIncomeModel.apply)(CurrentIncomeModel.unapply)
    )
  }

}
