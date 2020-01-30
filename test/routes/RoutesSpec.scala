/*
 * Copyright 2020 HM Revenue & Customs
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

package routes

import controllers.routes._
import org.scalatest._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class RoutesSpec extends UnitSpec with WithFakeApplication with Matchers {

  /* Outside Tax Years routes */
  "The URL for the resident/shares outside tax years Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/outside-tax-years" in {
      val path = GainController.outsideTaxYears().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/outside-tax-years"
    }
  }

  /* Disposal Date routes */
  "The URL for the resident/shares disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-date" in {
      val path = GainController.disposalDate().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }
  }

  "The URL for the resident/shares submit disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-date" in {
      val path = GainController.submitDisposalDate().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }
  }

  /* Sell for Less routes */
  "The URL for the resident/shares sellForLess Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/sell-for-less" in {
      val path = GainController.sellForLess().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/sell-for-less"
    }
  }

  "The URL for the resident/shares submit sellForLess Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/sell-for-less" in {
      val path = GainController.submitSellForLess().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/sell-for-less"
    }
  }

  /* Worth when Sold For Less routes */
  "The URL for the resident/shares worthWhenSoldForLess Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/worth-when-sold-for-less" in {
      val path = GainController.worthWhenSoldForLess().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/worth-when-sold-for-less"
    }
  }

  "The URL for the resident/shares submit worthWhenSoldForLess Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/worth-when-sold-for-less" in {
      val path = GainController.submitWorthWhenSoldForLess().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/worth-when-sold-for-less"
    }
  }

  /* Disposal Value routes */
  "The URL for the resident/shares disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-value" in {
      val path = GainController.disposalValue().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-value"
    }
  }

  "The URL for the resident/shares submit disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-value" in {
      val path = GainController.submitDisposalValue().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-value"
    }
  }

  /* Disposal Costs routes */
  "The URL for the resident/shares disposal costs Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-costs" in {
      val path = GainController.disposalCosts().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-costs"
    }
  }

  "The URL for the resident/shares submit disposal costs Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-costs" in {
      val path = GainController.submitDisposalCosts().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-costs"
    }
  }

  /* Shares Owner Before Legislation Start Routes */
  "The URL for the resident/shares owner before legislation start Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/owner-before-legislation-start" in {
      val path = GainController.ownerBeforeLegislationStart().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/owner-before-legislation-start"
    }
  }

  "The URL for the resident/shares submit owner before legislation start Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/owner-before-legislation-start" in {
      val path = GainController.submitOwnerBeforeLegislationStart().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/owner-before-legislation-start"
    }
  }

  /* Shares Value Before Legislation Start Routes */
  "The URL for the resident/shares value before legislation start Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/value-before-legislation-start" in {
      val path = GainController.valueBeforeLegislationStart().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/value-before-legislation-start"
    }
  }

  "The URL for the resident/shares submit value before legislation start Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/value-before-legislation-start" in {
      val path = GainController.submitValueBeforeLegislationStart().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/value-before-legislation-start"
    }
  }

  /* Did You Inherit the Shares Routes */
  "The URL for the resident/shares didYouInheritThem Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/did-you-inherit-the-shares" in {
      val path = GainController.didYouInheritThem().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/did-you-inherit-them"
    }
  }

  "The URL for the resident/shares submitDidYouInheritThem Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/did-you-inherit-the-shares" in {
      val path = GainController.submitDidYouInheritThem().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/did-you-inherit-them"
    }
  }

  /* Worth when Inherited the Shares Routes */
  "The URL for the resident/shares Worth When You Inherited Shares GET Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/worth-when-inherited" in {
      val path = GainController.worthWhenInherited().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/worth-when-inherited"
    }
  }

  "The URL for the resident/shares Worth When You Inherited Shares POST Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/worth-when-inherited" in {
      val path = GainController.submitWorthWhenInherited().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/worth-when-inherited"
    }
  }

  /* Acquisition Value routes */
  "The URL for the resident/shares acquisition value Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/acquisition-value" in {
      val path = GainController.acquisitionValue().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/acquisition-value"
    }
  }

  "The URL for the resident/shares submit acquisition value Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/acquisition-value" in {
      val path = GainController.submitAcquisitionValue().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/acquisition-value"
    }
  }

  /* Acquisition Costs action */
  "The URL for the resident shares acquisitionCosts action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/acquisition-costs" in {
      val path = GainController.acquisitionCosts().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/acquisition-costs"
    }
  }

  "The URL for the resident shares submitAcquisitionCosts action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/acquisition-costs" in {
      val path = GainController.submitAcquisitionCosts().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/acquisition-costs"
    }
  }

  /* Losses Brought Forward routes */
  "The URL for the lossesBroughtForward action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
      val path = DeductionsController.lossesBroughtForward().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward"
    }
  }

  "The URL for the submitLossesBroughtForward action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
      val path = DeductionsController.submitLossesBroughtForward().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward"
    }
  }

  /* Losses Brought Forward Value routes */
  "The URL for the resident/shares lossesBroughtForward Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
      val path = DeductionsController.lossesBroughtForwardValue().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward-value"
    }
  }

  "The URL for the resident/shares submitLossesBroughtForward Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
      val path = DeductionsController.submitLossesBroughtForwardValue().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward-value"
    }
  }

  /* Current Income routes */
  "The URL for the resident shares currentIncome action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/current-income" in {
      val path = IncomeController.currentIncome().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/current-income"
    }
  }

  "The URL for the resident shares submitCurrentIncome action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/current-income" in {
      val path = IncomeController.submitCurrentIncome().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/current-income"
    }
  }

  /* Personal Allowance routes */
  "The URL for the resident shares personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/personal-allowance" in {
      val path = IncomeController.personalAllowance().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/personal-allowance"
    }
  }

  "The URL for the resident shares submitPersonalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/personal-allowance" in {
      val path = IncomeController.submitPersonalAllowance().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/personal-allowance"
    }
  }

  /* Gain Summary PDF routes */
  "The URL for the gainSummaryReport action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/gain-report" in {
      val path = ReportController.gainSummaryReport().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/gain-report"
    }
  }

  /* Final Summary Report routes */
  "The URL for the finalSummaryReport action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/final-report" in {
      val path = ReportController.finalSummaryReport().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/final-report"
    }
  }

  /* Deductions Summary PDF routes */
  "The URL for the deductionsReport action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/deductions-report" in {
      val path = ReportController.deductionsReport().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/deductions-report"
    }
  }

  /* What Next Non-SA routes */
  "The URL for the whatNextNonSaGain action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/what-next-non-sa-gain" in {
      val path = WhatNextNonSaController.whatNextNonSaGain().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/what-next-non-sa-gain"
    }
  }

  "The URL for the whatNextNonSaLoss action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/what-next-non-sa-loss" in {
      val path = WhatNextNonSaController.whatNextNonSaLoss().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/what-next-non-sa-loss"
    }
  }

  /* What Next SA routes */
  "The URL for the whatNextSAOverFourTimesAEA action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/what-next-zero-gain-over-limit" in {
      WhatNextSAController.whatNextSAOverFourTimesAEA().url shouldEqual "/calculate-your-capital-gains/resident/shares/what-next-sa-no-gain-over-limit"
    }
  }

  "The URL for the whatNextSANoGain action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/what-next-no-gain" in {
      WhatNextSAController.whatNextSANoGain().url shouldEqual "/calculate-your-capital-gains/resident/shares/what-next-sa-no-gain"
    }
  }

  "The URL for the whatNextSAGain action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/what-next-gain" in {
      WhatNextSAController.whatNextSAGain().url shouldEqual "/calculate-your-capital-gains/resident/shares/what-next-sa-gain"
    }
  }

  /* SA user routes */
  "The URL for the saUser action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/confirm-self-assessment" in {
      val path = SaUserController.saUser().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/confirm-self-assessment"
    }
  }

  "The URL for the submitSaUser action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/confirm-self-assessment" in {
      val path = SaUserController.submitSaUser().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/confirm-self-assessment"
    }
  }
}
