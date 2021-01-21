/*
 * Copyright 2021 HM Revenue & Customs
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

package constructors

import assets.MessageLookup.{SummaryPage => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares.DeductionGainAnswersModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesProvider
import play.api.mvc.MessagesControllerComponents

class SummaryConstructorSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar with FakeRequestHelper {
  implicit val mockMessagesProvider = mock[MessagesProvider]
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val summaryConstructor = new SummaryConstructor()

  "Calling the .gainMessage function" when {

    "result is a loss" should {

      s"return the message '${messages.totalLoss}'" in {
        summaryConstructor.gainMessage(BigDecimal(-1000)) shouldBe messages.totalLoss
      }
    }

    "result is a gain" should {

      s"return the message '${messages.totalGain}'" in {
        summaryConstructor.gainMessage(BigDecimal(1000)) shouldBe messages.totalGain
      }
    }

    "result is 0" should {

      s"return the message '${messages.totalGain}'" in {
        summaryConstructor.gainMessage(BigDecimal(0)) shouldBe messages.totalGain
      }
    }
  }

  "Calling the .broughtForwardLossesUsed function" when {

    "no brought forward losses are claimed" should {
      lazy val answers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(false)), None)

      "return a value of '0'" in {
        summaryConstructor.broughtForwardLossesUsed(answers) shouldBe "0"
      }
    }

    "no brought forward losses are claimed with a provided value" should {
      lazy val answers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(false)),
        Some(LossesBroughtForwardValueModel(BigDecimal(10000))))

      "return a value of '0'" in {
        summaryConstructor.broughtForwardLossesUsed(answers) shouldBe "0"
      }
    }

    "brought forward losses are claimed with a provided value" should {
      lazy val answers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(true)),
        Some(LossesBroughtForwardValueModel(BigDecimal(10000))))

      "return a value of '10,000'" in {
        summaryConstructor.broughtForwardLossesUsed(answers) shouldBe "10,000"
      }
    }

    "brought forward losses are claimed with a provided value with decimal places" should {
      lazy val answers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(true)),
        Some(LossesBroughtForwardValueModel(BigDecimal(9999.99))))

      "return a value of '10,000' when rounded up" in {
        summaryConstructor.broughtForwardLossesUsed(answers) shouldBe "10,000"
      }
    }
  }
}
