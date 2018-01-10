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

package assets

import java.time.LocalDate
import common.Dates

import scala.concurrent.Future

object DateAsset {

  def getYearAfterCurrentTaxYear: Future[String] = {
    val now = LocalDate.now()
    val year = now.getYear
    if (now.isAfter(LocalDate.parse(s"${year.toString}-${Dates.taxYearEnd}"))) {
      Future.successful(Dates.taxYearToString(year + 2))
    }
    else {
      Future.successful(Dates.taxYearToString(year + 1))
    }
  }

}
