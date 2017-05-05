/*
 * Copyright 2017 HM Revenue & Customs
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

package models.resident

import play.api.libs.json.{Json, OFormat}

case class TaxYearModel (taxYearSupplied: String, isValidYear: Boolean, calculationTaxYear: String)

object TaxYearModel {
  implicit val formats: OFormat[TaxYearModel] = Json.format[TaxYearModel]

  def convertToSummaryFormat(taxYear: String): String = {
    val startYear = taxYear.take(4)
    val endYear = startYear.toInt + 1
    startYear + " to " + endYear
  }
}
