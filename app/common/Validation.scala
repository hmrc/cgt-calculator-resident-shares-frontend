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

package common

import java.time.{LocalDate, ZonedDateTime}

import common.Dates.constructDate
import models.resident.DisposalDateModel
import play.api.data.validation.{Invalid, ValidationError, _}
import uk.gov.hmrc.play.views.helpers.MoneyPounds

import scala.util.{Failure, Success, Try}

object Validation {

  def dateAfterMinimumConstraint(minDate: ZonedDateTime): Constraint[DisposalDateModel] = Constraint({
    value => dateAfterMinimum(value.day, value.month, value.year, minDate.toLocalDate)
  })

  def maxMonetaryValueConstraint(
                                  maxValue: BigDecimal,
                                  errMsgKey: String = "calc.common.error.maxNumericExceeded"
                                ): Constraint[BigDecimal] = Constraint("constraints.maxValue")({
    value => maxMoneyCheck(value, maxValue, errMsgKey)
  })

  def maxMonetaryValueConstraint[T](maxValue: BigDecimal, extractMoney: T => Option[BigDecimal]): Constraint[T] = {
    Constraint("constraints.maxValueCustom")({
      data => extractMoney(data).map {
        maxMoneyCheck(_, maxValue, "calc.common.error.maxNumericExceeded")
      }.getOrElse(Valid)
    })
  }

  private def maxMoneyCheck(value: BigDecimal, maxValue: BigDecimal, errMsgKey: String): ValidationResult = {
    if(value <= maxValue) {
      Valid
    } else {
      Invalid(ValidationError(errMsgKey, MoneyPounds(maxValue, 0).quantity))
    }
  }

  def dateAfterMinimum(day: Int, month: Int, year: Int, minimumDate: LocalDate): ValidationResult = {
    if(isValidDate(day, month, year) && constructDate(day, month, year).isAfter(minimumDate)){
      Valid
    } else {
      Invalid(ValidationError("calc.common.date.error.beforeMinimum", s"${minimumDate.getDayOfMonth} ${minimumDate.getMonth} ${minimumDate.getYear}"))
    }
  }

  def isValidDate(day: Int, month: Int, year: Int): Boolean = Try(constructDate(day, month, year)) match {
    case Success(_) => true
    case _ => false
  }

  val bigDecimalCheck: String => Boolean = input => Try(BigDecimal(input)) match {
    case Success(_) => true
    case Failure(_) if input.trim == "" => true
    case Failure(_) => false
  }

  val integerCheck: String => Boolean = input => Try(input.trim.toInt) match {
    case Success(_) => true
    case Failure(_) if input.trim == "" => true
    case Failure(_) => false
  }

  val mandatoryCheck: String => Boolean = input => input.trim != ""

  val decimalPlacesCheck: BigDecimal => Boolean = input => input.scale < 3

  val decimalPlacesCheckNoDecimal: BigDecimal => Boolean = input => input.scale < 1

  val validYearRangeCheck: Int => Boolean = input => input >= 1900 && input <= 9999

  val maxCheck: BigDecimal => Boolean = input => input <= Constants.maxNumeric

  val isPositive: BigDecimal => Boolean = input => input >= 0

  val yesNoCheck: String => Boolean = {
    case "Yes" => true
    case "No" => true
    case "" => true
    case _ => false
  }

  val optionalMandatoryCheck: Option[String] => Boolean = {
    case Some(input) => mandatoryCheck(input)
    case _ => false
  }

  val optionalYesNoCheck: Option[String] => Boolean = {
    case Some(input) => yesNoCheck(input)
    case _ => true
  }

  val optionStringToBoolean: Option[String] => Boolean = {
    case Some("Yes") => true
    case _ => false
  }

  val booleanToOptionString: Boolean => Option[String] = input => {
    if (input) Some("Yes")
    else Some("No")
  }
}
