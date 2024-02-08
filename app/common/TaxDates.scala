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

package common

import java.time.LocalDate

import common.Dates.formatter

object TaxDates {
  val legislationDate: LocalDate = LocalDate.parse("1/4/1982", formatter)
  val taxStartDate: LocalDate = LocalDate.parse("5/4/2015", formatter)
  val taxStartDatePlus18Months: LocalDate = LocalDate.parse("5/10/2016", formatter)
  val taxYearStartDate: LocalDate = LocalDate.parse("5/4/2016", formatter)
  val taxYearEndDate: LocalDate = LocalDate.parse("5/4/2017", formatter)

  def dateAfterStart(date: LocalDate): Boolean = date.isAfter(taxStartDate)

  def taxYearStringToInteger(taxYear: String): Int = (taxYear.take(2) + taxYear.takeRight(2)).toInt

}
