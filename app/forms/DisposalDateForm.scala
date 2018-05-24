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
import models.resident.DisposalDateModel
import java.time.{LocalDate, ZoneId, ZonedDateTime}

import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object DisposalDateForm {

  def disposalDateForm(minimumDate: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"))): Form[DisposalDateModel] = Form(
    mapping(
      "disposalDateDay" -> text
        .verifying("calc.resident.disposalDate.invalidDayError", mandatoryCheck)
        .verifying("calc.resident.disposalDate.invalidDayError", integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "disposalDateMonth" -> text
        .verifying("calc.resident.disposalDate.invalidMonthError", mandatoryCheck)
        .verifying("calc.resident.disposalDate.invalidMonthError", integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "disposalDateYear" -> text
        .verifying("calc.resident.disposalDate.invalidYearError", mandatoryCheck)
        .verifying("calc.resident.disposalDate.invalidYearError", integerCheck)
        .transform[Int](stringToInteger, _.toString)
        .verifying("calc.resident.disposalDate.invalidYearRangeError", validYearRangeCheck)
    )(DisposalDateModel.apply)(DisposalDateModel.unapply)
      .verifying("calc.common.date.error.invalidDate", fields => isValidDate(fields.day, fields.month, fields.year))
      .verifying(Messages("calc.common.date.error.beforeMinimum", s"${minimumDate.getDayOfMonth} ${minimumDate.getMonth} ${minimumDate.getYear}"),
        fields => dateAfterMinimum(fields.day, fields.month, fields.year, minimumDate.toLocalDate))
  )
}
