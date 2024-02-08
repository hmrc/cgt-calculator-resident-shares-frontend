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

import common.Transformers._
import common.Validation._
import models.resident.income.PersonalAllowanceModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{Lang, MessagesApi}

import javax.inject.Inject

class PersonalAllowanceForm @Inject()(implicit val messagesApi: MessagesApi) {

  def validateMaxPA (maxPersonalAllowance: BigDecimal): BigDecimal => Boolean = {
    input => if(input > maxPersonalAllowance) false else true
  }

  def apply(maxPA: BigDecimal = BigDecimal(0), taxYear: String, lang: Lang): Form[PersonalAllowanceModel] = Form(
    mapping(
      "amount" -> text
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying(messagesApi("calc.resident.personalAllowance.error.mandatoryAmount", taxYear)(lang), mandatoryCheck)
        .verifying(messagesApi("calc.resident.personalAllowance.error.invalidAmount", taxYear)(lang), bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(maxMonetaryValueConstraint(maxPA))
        .verifying(messagesApi("calc.resident.personalAllowance.error.minimumAmount", taxYear)(lang), isPositive)
        .verifying(messagesApi("calc.resident.personalAllowance.error.invalidAmount", taxYear)(lang), decimalPlacesCheckNoDecimal)
    )(PersonalAllowanceModel.apply)(PersonalAllowanceModel.unapply)
  )

}
