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

package common

import scala.util.{Failure, Success, Try}

object Transformers {

  val stringToBigDecimal: String => BigDecimal = (input) => Try(BigDecimal(input.trim)) match {
    case Success(value) => value
    case Failure(_) => BigDecimal(0)
  }

  val bigDecimalToString: BigDecimal => String = (input) => input.scale match {
    case 1 => input.setScale(2).toString()
    case _ => input.toString
  }

  val stringToInteger: String => Int = (input) => Try(input.trim.toInt) match {
    case Success(value) => value
    case Failure(_) => 0
  }

  val stringToBoolean: String => Boolean = {
    case "Yes" => true
    case _ => false
  }

  val booleanToString: Boolean => String = (input) => if (input) "Yes" else "No"
}
