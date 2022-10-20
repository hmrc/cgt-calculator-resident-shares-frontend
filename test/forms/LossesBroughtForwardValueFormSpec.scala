/*
 * Copyright 2022 HM Revenue & Customs
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

import assets.MessageLookup.{Resident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models.resident.LossesBroughtForwardValueModel
import play.api.i18n.{Lang, MessagesApi}

class LossesBroughtForwardValueFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  val injectedForm = fakeApplication.injector.instanceOf[LossesBroughtForwardValueForm]
  val lossesBroughtForwardValueForm = injectedForm("2022", Lang("en"))
  implicit val messagesApi = fakeApplication.injector.instanceOf[MessagesApi]

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      val model = LossesBroughtForwardValueModel(1)
      val form = lossesBroughtForwardValueForm.fill(model)
      form.data("amount") shouldBe "1"
    }
  }

  "Creating a form using map" when {

    "supplied with valid data" should {
      lazy val form = lossesBroughtForwardValueForm.bind(Map(("amount", "1000")))

      "return a form with the mapped data" in {
        form.get shouldBe LossesBroughtForwardValueModel(BigDecimal(1000))
      }
    }

    "supplied with no data for amount" should {

      lazy val form = lossesBroughtForwardValueForm.bind(Map("amount" -> ""))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.mandatoryAmount}'" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.lossesBroughtForwardValue.error.mandatoryAmount", "2022")(Lang("en"))
      }
    }

    "supplied with non-numeric data for amount" should {

      lazy val form = lossesBroughtForwardValueForm.bind(Map("amount" -> "a"))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.invalidAmount}'" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.lossesBroughtForwardValue.error.invalidAmount", "2022")(Lang("en"))
      }
    }

    "supplied with an amount that is too big" should {

      lazy val form = lossesBroughtForwardValueForm.bind(Map("amount" -> "9999999999999"))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.maximumAmount}'" in {
        form.error("amount").get.message shouldBe "calc.common.error.maxAmountExceeded"
      }
    }

    "supplied with a negative amount" should {

      lazy val form = lossesBroughtForwardValueForm.bind(Map("amount" -> "-1000"))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.minimumAmount}'" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.lossesBroughtForwardValue.error.minimumAmount", "2022")(Lang("en"))
      }
    }

    "supplied with an amount that has too many decimal places" should {

      lazy val form = lossesBroughtForwardValueForm.bind(Map("amount" -> "1000.001"))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.invalidAmount}'" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.lossesBroughtForwardValue.error.invalidAmount", "2022")(Lang("en"))
      }
    }
  }
}
