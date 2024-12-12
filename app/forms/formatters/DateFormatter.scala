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

package forms.formatters

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.{ChronoField, ValueRange}
import scala.util.{Failure, Success, Try}

case class DateFormatter(key: String,
                         optMinDate: Option[LocalDate] = None,
                         optMaxDate: Option[LocalDate] = None,
                         rangeInclusive: Boolean = false
                        )(implicit val messages: Messages) extends Formatter[LocalDate] {

  private lazy val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", messages.lang.toLocale)

  private val dateRequiredError     = s"$key.error.required"
  private val dayRequiredError      = s"$key.error.required.day"
  private val dayMonthRequiredError = s"$key.error.required.dayMonth"
  private val dayYearRequiredError  = s"$key.error.required.dayYear"
  private val monthRequiredError    = s"$key.error.required.month"
  private val monthYearRequiredError= s"$key.error.required.monthYear"
  private val yearRequiredError     = s"$key.error.required.year"

  private val dateInvalidError      = s"$key.error.invalid"
  private val dayInvalidError       = s"$key.error.invalid.day"
  private val dayMonthInvalidError  = s"$key.error.invalid.dayMonth"
  private val dayYearInvalidError   = s"$key.error.invalid.dayYear"
  private val monthInvalidError     = s"$key.error.invalid.month"
  private val monthYearInvalidError = s"$key.error.invalid.monthYear"
  private val yearInvalidError      = s"$key.error.invalid.year"

  private val dateNotRealError      = s"$key.error.notReal"
  private val dayNotRealError       = s"$key.error.notReal.day"
  private val monthNotRealError     = s"$key.error.notReal.month"
  private val yearNotRealError      = s"$key.error.notReal.year"

  private val dateMinError          = s"$key.error.range.min"
  private val dateMaxError          = s"$key.error.range.max"

  private val dayKey    = s"$key.day"
  private val monthKey  = s"$key.month"
  private val yearKey   = s"$key.year"

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    // Replaces Some("") with None
    val optDay = data.get(s"$key.day").filter(_.nonEmpty)
    val optMonth = data.get(s"$key.month").filter(_.nonEmpty)
    val optYear = data.get(s"$key.year").filter(_.nonEmpty)

    for {
      fields <- nonEmptyFields(optDay, optMonth, optYear)
      (dayField, monthField, yearField) = fields
      validFields <- validateFields(dayField, monthField, yearField)
      (day, month, year) = validFields
      validDate <- validateDate(day, month, year)
      inRangeDate <- dateWithinRange(validDate)
    } yield inRangeDate
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] = Map(
    s"$key.day" -> value.getDayOfMonth.toString,
    s"$key.month" -> value.getMonthValue.toString,
    s"$key.year" -> value.getYear.toString
  )

  private def nonEmptyFields(optDay: Option[String], optMonth: Option[String], optYear: Option[String]): Either[Seq[FormError], (String, String, String)] = {
    (optDay, optMonth, optYear) match {
      case (Some(day),  Some(month),  Some(year)) => Right((day, month, year))
      case (None,       Some(_),      Some(_)   ) => Left(Seq(FormError(dayKey, dayRequiredError)))
      case (None,       None,         Some(_)   ) => Left(Seq(FormError(dayKey, dayMonthRequiredError), FormError(monthKey, dayMonthRequiredError)))
      case (None,       Some(_),      None      ) => Left(Seq(FormError(dayKey, dayYearRequiredError), FormError(yearKey, dayYearRequiredError)))
      case (Some(_),    None,         Some(_)   ) => Left(Seq(FormError(monthKey, monthRequiredError)))
      case (Some(_),    None,         None      ) => Left(Seq(FormError(monthKey, monthYearRequiredError), FormError(yearKey, monthYearRequiredError)))
      case (Some(_),    Some(_),      None      ) => Left(Seq(FormError(yearKey, yearRequiredError)))
      case (None,       None,         None      ) => Left(Seq(FormError(key, dateRequiredError)))
    }
  }

  private def validateFields(dayField: String, monthField: String, yearField: String): Either[Seq[FormError], (Int, Int, Int)] = {
    (dayField.toIntOption, monthField.toIntOption, yearField.toIntOption) match {
      case (Some(day),  Some(month),  Some(year)) => Right((day, month, year))
      case (None,       Some(_),      Some(_)   ) => Left(Seq(FormError(dayKey, dayInvalidError)))
      case (None,       None,         Some(_)   ) => Left(Seq(FormError(dayKey, dayMonthInvalidError), FormError(monthKey, dayMonthInvalidError)))
      case (None,       Some(_),      None      ) => Left(Seq(FormError(dayKey, dayYearInvalidError), FormError(yearKey, dayYearInvalidError)))
      case (Some(_),    None,         Some(_)   ) => Left(Seq(FormError(monthKey, monthInvalidError)))
      case (Some(_),    None,         None      ) => Left(Seq(FormError(monthKey, monthYearInvalidError), FormError(yearKey, monthYearInvalidError)))
      case (Some(_),    Some(_),      None      ) => Left(Seq(FormError(yearKey, yearInvalidError)))
      case (None,       None,         None      ) => Left(Seq(FormError(key, dateInvalidError)))
    }
  }

  private def validateDate(day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] = {
    val validatedDay = Try(ChronoField.DAY_OF_MONTH.checkValidIntValue(day)).toOption

    val validatedMonth = Try(ChronoField.MONTH_OF_YEAR.checkValidIntValue(month)).toOption

    val validatedYear = Try(ValueRange.of(1000, 9999).checkValidIntValue(year, ChronoField.YEAR)).toOption

    (validatedDay, validatedMonth, validatedYear) match {
      case (Some(_), Some(_), Some(_)) => Try(LocalDate.of(year, month, day)) match {
        case Success(date) =>
          Right(date)
        case Failure(_) =>
          Left(Seq(FormError(key, dateNotRealError)))
      }
      case (None, Some(_), Some(_)) => Left(Seq(FormError(dayKey, dayNotRealError)))
      case (Some(_), None, Some(_)) => Left(Seq(FormError(monthKey, monthNotRealError)))
      case (Some(_), Some(_), None) => Left(Seq(FormError(yearKey, yearNotRealError)))
      case _ => Left(Seq(FormError(key, dateNotRealError)))
    }
  }

  private def dateWithinRange(date: LocalDate): Either[Seq[FormError], LocalDate] = {
    if (optMinDate.exists(min => date.isBefore(min) || (!rangeInclusive && date.isEqual(min)))) {
      Left(Seq(FormError(key, dateMinError, optMinDate.toSeq.map(formatter.format))))
    } else if (optMaxDate.exists(max => date.isAfter(max) || (!rangeInclusive && date.isEqual(max)))) {
      Left(Seq(FormError(key, dateMaxError, optMaxDate.toSeq.map(formatter.format))))
    } else {
      Right(date)
    }
  }

}
