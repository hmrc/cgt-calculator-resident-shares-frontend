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

import forms.formatters.DateFormatter
import models.resident.DisposalDateModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

import java.time.LocalDate

object DisposalDateForm {

  val key = "disposalDate"

  def disposalDateForm(minimumDate: LocalDate = LocalDate.now)(implicit messages: Messages): Form[DisposalDateModel] = Form(
    mapping(
      key -> of(DateFormatter(
        key,
        optMinDate = Some(minimumDate)
      ))
    )(date => DisposalDateModel(date.getDayOfMonth, date.getMonthValue, date.getYear))(model => Some(LocalDate.of(model.year, model.month, model.day)))
  )
}
