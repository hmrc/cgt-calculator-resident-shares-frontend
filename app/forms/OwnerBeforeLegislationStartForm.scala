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
import models.resident.shares.OwnerBeforeLegislationStartModel
import play.api.data.Form
import play.api.data.Forms._

object OwnerBeforeLegislationStartForm {

  lazy val ownerBeforeLegislationStartForm = Form(
    mapping(
      "ownerBeforeLegislationStart" -> text
        .verifying("calc.resident.shares.ownerBeforeLegislationStart.noSelectError", mandatoryCheck)
        .verifying("calc.resident.shares.ownerBeforeLegislationStart.noSelectError", yesNoCheck)
        .transform(stringToBoolean, booleanToString)
    )(OwnerBeforeLegislationStartModel.apply)(o=>Some(o.ownerBeforeLegislationStart))
  )
}
