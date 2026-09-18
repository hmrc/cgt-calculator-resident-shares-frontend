/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError}

object Formatters {
  def text(errorKey: String = "error.required", optional: Boolean = false): FieldMapping[String] =
    of(using stringFormatter(errorKey, optional))

  private def stringFormatter(errorKey: String, optional: Boolean): Formatter[String] = new Formatter[String] {

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None => Left(Seq(FormError(key, errorKey)))
        case Some(x) if x.trim.isEmpty && !optional => Left(Seq(FormError(key, errorKey)))
        case Some(x) if x.trim.isEmpty && optional => Right(x.trim)
        case Some(s) => Right(s.trim)
      }

    def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value.trim)
  }
}
