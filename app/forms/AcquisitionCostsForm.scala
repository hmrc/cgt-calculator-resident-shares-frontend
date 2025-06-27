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
import models.resident.AcquisitionCostsModel
import play.api.data.Form
import play.api.data.Forms._

object AcquisitionCostsForm {

  lazy val acquisitionCostsForm = Form(
    mapping(
      "amount" -> text
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying("calc.resident.shares.acquisitionCosts.error.mandatoryAmount", mandatoryCheck)
        .verifying("calc.resident.shares.acquisitionCosts.error.invalidAmount", bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(maxMonetaryValueConstraint(Constants.maxNumeric, "calc.resident.shares.acquisitionCosts.error.maxAmountExceeded"))
        .verifying("calc.resident.shares.acquisitionCosts.error.minimumAmount", isPositive)
        .verifying("calc.resident.shares.acquisitionCosts.error.invalidDecimalPlace", decimalPlacesCheck)
    )(AcquisitionCostsModel.apply)(o=>Some(o.amount))
  )
}
