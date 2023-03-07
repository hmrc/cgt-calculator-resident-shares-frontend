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
import models.resident.WorthWhenInheritedModel
import play.api.data.Form
import play.api.data.Forms._

object WorthWhenInheritedForm {

  lazy val worthWhenInheritedForm = Form(
    mapping(
      "amount" -> text
        .verifying("calc.resident.shares.worthWhenInherited.error.mandatoryAmount", mandatoryCheck)
        .verifying("calc.resident.shares.worthWhenInherited.error.invalidAmount", bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(maxMonetaryValueConstraint(Constants.maxNumeric))
        .verifying("calc.resident.shares.worthWhenInherited.error.minimumAmount", isPositive)
        .verifying("calc.resident.shares.worthWhenInherited.error.invalidAmount", decimalPlacesCheck)
    )(WorthWhenInheritedModel.apply)(WorthWhenInheritedModel.unapply)
  )
}
