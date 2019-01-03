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

package forms

import common.Validation._
import models.resident.SaUserModel
import play.api.data.Form
import play.api.data.Forms._

object SaUserForm {

  val saUserForm = Form(
    mapping (
      "isInSa" -> optional(text)
        .verifying("calc.resident.saUser.errorSelect", optionalMandatoryCheck)
        .verifying("calc.resident.saUser.errorSelect", optionalYesNoCheck)
        .transform[Boolean](optionStringToBoolean, booleanToOptionString)
    )(SaUserModel.apply)(SaUserModel.unapply)
  )
}
