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

package helpers

import org.jsoup.select.Elements
import org.scalatest.Assertions.cancel

trait AssertHelpers {

  //requires a test for a Some result before using this assert
  def assertOption[T](message: String)(option: Option[T])(test: T => Unit): Unit = {
    option.fold(cancel(message)) { value =>
      test(value)
    }
  }

  //requires a test to validate a non-empty array before using this assert
  def assertHTML(elements: Elements)(test: Elements => Unit): Unit = {
    if(elements.isEmpty) cancel("element not found")
    else test(elements)
  }

}
