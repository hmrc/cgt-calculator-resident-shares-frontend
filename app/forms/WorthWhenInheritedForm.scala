/*
 * Copyright 2016 HM Revenue & Customs
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
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object WorthWhenInheritedForm {

  lazy val worthWhenInheritedForm = Form(
    mapping(
      "amount" -> text
        .verifying(Messages("calc.common.error.mandatoryAmount"), mandatoryCheck)
        .verifying(Messages("calc.common.error.invalidAmount"), bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(Messages("calc.common.error.maxAmountExceeded", MoneyPounds(Constants.maxNumeric, 0).quantity), maxCheck)
        .verifying(Messages("calc.common.error.minimumAmount"), isPositive)
        .verifying(Messages("calc.common.error.invalidAmount"), decimalPlacesCheck)
    )(WorthWhenInheritedModel.apply)(WorthWhenInheritedModel.unapply)
  )
}
