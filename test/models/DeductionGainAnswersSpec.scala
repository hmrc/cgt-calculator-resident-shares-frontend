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

package models

import common.Dates._
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import uk.gov.hmrc.play.test.UnitSpec

class DeductionGainAnswersSpec extends UnitSpec {

  "Creating a Gain model for shares to display worth when bought" should {
    val model = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(10),
      worthWhenSoldForLess = None,
      disposalCosts = 20,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(30),
      acquisitionCosts = 40
    )

    "return a result of true for displayWorthWhenBought" in {
      val result = model.displayWorthWhenBought

      result shouldBe true
    }
  }

  "Creating a Gain model for shares to display worth when inherited" should {
    val model = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(10),
      worthWhenSoldForLess = None,
      disposalCosts = 20,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(true),
      worthWhenInherited = Some(100000),
      acquisitionValue = Some(30),
      acquisitionCosts = 40
    )

    "return a result of true for displayWorthWhenInherited" in {
      val result = model.displayWorthWhenInherited

      result shouldBe true
    }
  }
}
