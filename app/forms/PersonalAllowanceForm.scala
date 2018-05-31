/*
 * Copyright 2018 HM Revenue & Customs
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

object PersonalAllowanceForm {

  def validateMaxPA (maxPersonalAllowance: BigDecimal): BigDecimal => Boolean = {
    input => if(input > maxPersonalAllowance) false else true
  }

  def personalAllowanceForm(maxPA: BigDecimal = BigDecimal(0)): Form[PersonalAllowanceModel] = Form(
    mapping(
      "amount" -> text
        .verifying("calc.common.error.mandatoryAmount", mandatoryCheck)
        .verifying("calc.common.error.invalidAmountNoDecimal", bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, _.toString())
        .verifying(maxMonetaryValueConstraint(maxPA))
        .verifying("calc.common.error.minimumAmount", isPositive)
        .verifying("calc.common.error.invalidAmountNoDecimal", decimalPlacesCheckNoDecimal)
    )(PersonalAllowanceModel.apply)(PersonalAllowanceModel.unapply)
  )

}
