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

package assets

object MessageLookup {

  // TO MOVE

  trait Common {

    val change = "Change"
    val back = "Back"
    val continue = "Continue"
    val yes = "Yes"
    val no = "No"
    val day = "Day"
    val month = "Month"
    val year = "Year"

    val mandatoryAmount = "Enter an amount"
    val minimumAmount = "Enter an amount that's £0 or more"
    val maximumAmount = "Enter an amount that's £1,000,000,000 or less"

    def maximumLimit(limit: String): String = s"Enter an amount that's £$limit or less"

    val invalidAmount = "Enter an amount in the correct format"
    val invalidDecimalPlace = "Enter an amount in the correct decimal places"
    val invalidAmountNoDecimal = "Enter an amount in the correct format"
  }

  object Resident extends Common {

    val homeText = "Calculate your Capital Gains Tax"
    val errorInvalidDate = "Enter a real date"

    object Shares {

      //This object will have some duplication of text from the properties summary as well as duplicating
      //some of the questions for the shares pages however it will still pull form the same messages.en location
      //this is to encourage making the changes in the tests first in both places and understanding what changing
      //the message will affect.

      object ReviewAnswers {
        val title = "Check your answers - Calculate your Capital Gains Tax - GOV.UK"
        val heading = "Check your answers"
      }

      object SharesSummaryMessages {
        val disposalDateQuestion = "When did you sell or give away the shares?"
        val disposalValueQuestion = "How much did you sell the shares for?"
        val disposalCostsQuestion = "How much did you pay in costs when you sold the shares?"
        val acquisitionValueQuestion = "How much did you pay for the shares?"
        val acquisitionCostsQuestion = "How much did you pay in costs when you got the shares?"
      }

      object ValueBeforeLegislationStart {
        val h1 = "Market value of the shares on 31 March 1982"
        val title = s"$h1 - Calculate your Capital Gains Tax - GOV.UK"
        val question = "What were the shares worth on 31 March 1982?"
        val helpText = "Get valuation information from the stock exchange or talk to your stockbroker or fund manager."
        val hintText = "If you owned the shares with someone else, only enter the value of your portion of the shares."
      }

      object DisposalValue {
        val h1 = "Shares value when sold"
        val title = s"$h1 - Calculate your Capital Gains Tax - GOV.UK"
        val question = "How much did you sell the shares for?"
        val jointOwnership = "If you owned the shares with someone else, only enter your portion of the sale value."
      }

      //############ Owner Before Legislation Start messages.en #################//
      object OwnerBeforeLegislationStart {
        val title = "Did you own the shares before 1 April 1982? - Calculate your Capital Gains Tax - GOV.UK"
        val heading = "Did you own the shares before 1 April 1982?"
        val errorNoSelect = "Tell us if you owned the shares before 1 April 1982"
      }

      object DidYouInheritThem {
        val question = "Did you inherit the shares?"
      }

      //############ Sell For Less messages.en #################//
      object SellForLess {
        val title = "Did you sell the shares for less than they were worth to help the buyer?"
        val newTitle = "Did you sell the shares for less than they were worth to help the buyer? - Calculate your Capital Gains Tax - GOV.UK"
      }

      //############ Worth When Inherited messages.en #################//
      object WorthWhenInherited {
        val h1 = "Shares value when inherited"
        val title = s"$h1 - Calculate your Capital Gains Tax - GOV.UK"
        val question = "What were the shares worth when you inherited them?"
        val helpText = "Use information from the stock exchange or talk to your stockbroker or fund manager."
        val hintText = "If you owned the shares with someone else, only enter the market value for your portion of the shares."
      }

      //############ Worth When Sold For Less messages.en #################//
      object WorthWhenSoldForLess {
        val h1 = "Market value when sold"
        val title = s"$h1 - Calculate your Capital Gains Tax - GOV.UK"
        val question = "What were the shares worth when you sold them?"
        val informationText = "Get information from the stock exchange or talk to your stockbroker or fund manager."
        val jointOwnershipText = "If you owned the shares with someone else, only enter the value of your portion of the shares."
      }
    }
  }

  //Disposal Date messages.en
  object DisposalDate {
    val helpText = "For example, 4 9 2021"
    val requiredDateError = "Enter the day, month and year you sold or gave away the shares"
    val requiredDayError = "Enter the day you sold or gave away the shares"
    val invalidDateError = "Enter a number, in digits, for the day, month and year you sold or gave away the shares"
    val realDateError = "Enter a real date for when you sold or gave away the shares"
    val invalidYearRange = "Enter a real year for when you sold or gave away the shares"
    val invalidMinimumDate = s"The date you sold or gave away the shares must be after 5 April 2015"
  }

  //Outside Tax Years messages.en
  object OutsideTaxYears {
    val title = "The date you have entered is not supported by this calculator - Calculate your Capital Gains Tax - GOV.UK"
    val heading = "The date you have entered is not supported by this calculator"
    val tooEarly = "You can use this calculator if you have sold a property since 5 April 2015."
    val sharesTooEarly = "You can use this calculator if you have sold shares since 5 April 2015."

    def content(year: String): String = s"You can continue to use it, but we will use the tax rates from the $year tax year."
  }

  //Summary messages.en
  object SummaryPage {
    def title(taxYear: String) = s"Capital Gains Tax to pay for the $taxYear tax year - Calculate your Capital Gains Tax - GOV.UK"

    val print = "Print your Capital Gains Tax calculation"
  }

  //Losses Brought Forward messages.en
  object LossesBroughtForward {
    def title(input: String): String = s"Are you claiming any Capital Gains Tax losses from tax years before $input? - Calculate your Capital Gains Tax - GOV.UK"

    def question(input: String): String = s"Are you claiming any Capital Gains Tax losses from tax years before $input?"

    val helpText = "These are unused losses that are covered by Capital Gains Tax and that you have already reported to HMRC."
  }

  //Losses Brought Forward Value messages.en
  object LossesBroughtForwardValue {
    def title(input: String): String = s"What is the total value of your Capital Gains Tax losses from tax years before $input? - Calculate your Capital Gains Tax - GOV.UK"

    def question(input: String): String = s"What is the total value of your Capital Gains Tax losses from tax years before $input?"
  }

  //Current Income messages.en
  object CurrentIncome {
    def title(input: String): String = s"In the $input tax year, what was your income? - Calculate your Capital Gains Tax - GOV.UK"

    def question(input: String): String = s"In the $input tax year, what was your income?"

    val currentYearTitle = "How much do you expect your income to be in this tax year? - Calculate your Capital Gains Tax - GOV.UK"
    val helpText = "Include your salary before tax, and anything else you pay income tax on, but not the money you made from selling the property."
    val helpTextShares = "Include your salary before tax, and anything else you pay income tax on, but not the money you made from selling the shares. For example, £10,000.50"
  }

  //Personal Allowance messages.en
  object PersonalAllowance {
    val h1: String => String = (input: String) => s"Personal Allowance in the $input tax year"
    val title: String => String = (input: String) => s"Personal Allowance in the $input tax year - Calculate your Capital Gains Tax - GOV.UK"
    def question(input: String): String = s"In the $input tax year, what was your Personal Allowance?"
    val link = "Income tax rates and Personal Allowances (opens in new tab)"
    val linkText = "Find out more about"
    val help = "This is the amount of your income that you do not pay tax on."
    def listTitle(yearOne: String, yearTwo: String, value: String): String =
      s"In the tax year $yearOne to $yearTwo the UK Personal Allowance was $value unless you:"
    val listOne = "earned more than £100,000"
    val listTwo = "claimed Blind Person's Allowance"
    val inYearQuestion = "How much is your Personal Allowance?"
  }

  //############ Shares messages.en ##############//
  object SharesDisposalDate {
    val title = "When did you sell or give away the shares? - Calculate your Capital Gains Tax - GOV.UK"
    val question = "When did you sell or give away the shares?"
  }

  object SharesAcquisitionCosts {
    val h1 = "Costs when you got the shares"
    val title = s"$h1 - Calculate your Capital Gains Tax - GOV.UK"
    val question = "How much did you pay in costs when you got the shares?"
    val helpText = "This includes costs for stockbroker fees and Stamp Duty."
    val hintText = "If you owned the shares with someone else, only enter your portion of the costs as agreed with your co-owner."
  }

  object SharesDisposalCosts {
    val h1 = "Costs when you sold the shares"
    val title = s"$h1 - Calculate your Capital Gains Tax - GOV.UK"
    val question = "How much did you pay in costs when you sold the shares?"
    val helpText = "This includes costs for stockbroker fees."
    val jointOwnership = "If you owned the shares with someone else, only enter your portion of the costs as agreed with your co-owner."
  }

  object SharesAcquisitionValue {
    val h1 = "What you paid for the shares"
    val title = s"$h1 - Calculate your Capital Gains Tax - GOV.UK"
    val question = "How much did you pay for the shares?"
    val hintText = "If you owned them with someone else, only enter your share of the purchase."
  }

  object SummaryDetails extends Common {
    def title(taxYear: String) = s"Capital Gains Tax to pay for the $taxYear tax year - Calculate your Capital Gains Tax - GOV.UK"

    def cgtToPay(taxYear: String): String = s"Capital Gains Tax to pay for the $taxYear tax year"

    val howWeWorkedThisOut = "How we have worked this out"
    val yourTotalGain = "Your total gain"
    val yourTotalLoss = "Your total loss"
    val disposalValue = "Value when you sold the shares"
    val acquisitionValue = "Minus the value of the shares when you acquired them"
    val acquisitionValueBeforeLegislation = "Minus the value of the shares at 31 March 1982"
    val totalCosts = "Minus all costs"
    val totalGain = "Total gain"
    val totalLoss = "Total loss"
    val yourDeductions = "Your deductions"
    val aeaUsed = "Capital Gains Tax Annual Exempt Amount used"
    val broughtForwardLossesUsed = "Loss used from previous tax years"
    val totalDeductions = "Total deductions"
    val yourTaxableGain = "Your taxable gain"
    val minusDeductions = "Minus deductions"
    val taxableGain = "Taxable gain"
    val yourTaxRate = "Your tax rate based on your Income Tax bands"

    def taxRate(amount: String, rate: String): String = s"$amount taxable gain multiplied by $rate% tax rate"

    val taxToPay = "Tax to pay"
    val print = "Print your Capital Gains Tax calculation"
    val remainingDeductions = "Your remaining deductions"

    def remainingAnnualExemptAmount(taxYear: String): String = s"Annual Exempt Amount left for the $taxYear tax year"

    val lossesToCarryForwardFromCalculation = "Losses you can carry forward from this calculation"
    val broughtForwardLossesRemaining = "Losses to carry forward from previous tax years"
    val whatToDoNext = "What happens next"
    val whatToDoNextDetails = "Before you continue, save a copy of your calculation. You will need this when you report your Capital Gains Tax."
    val whatToDoNextHeading = "What happens next"
    val whatToDoNextContinue = "Before you continue, save a copy of your calculation. You will need this when you report your Capital Gains Tax."
    val noticeSummary: String = "Warning Your result may be slightly inaccurate because the calculator does not support the dates you entered." +
      " Do not use these figures to report your Capital Gains Tax."

    val bannerPanelTitle = "Help improve HMRC services"
    val bannerPanelLinkText = "Sign up to take part in user research (opens in new tab)"
    val bannerPanelCloseVisibleText = "No thanks"
  }

  object WhatNextPages {
    val title = "What happens next - Calculate your Capital Gains Tax - GOV.UK"
    val heading = "What happens next"
    val finish = "Return to GOV.UK"
    val reportNow = "Report now"
    val yourOptionsTitle = "Your options for reporting your Capital Gains Tax - Calculate your Capital Gains Tax - GOV.UK"
    val yourOptionHEading = "Your options for reporting your Capital Gains Tax"

    object FourTimesAEA {
      val paragraphOne = s"You will have to report your Capital Gains Tax figures through Self Assessment."
      val paragraphTwo = "You must do this, even though there is no tax to pay."
    }

    object WhatNextGain {
      val bulletPointTitle = "You can either:"
      val bulletPointOne: String => String = year => s"report and pay on your Self Assessment return for the $year tax year"
      val bulletPointTwo = "report and pay now using our online form"
      val importantInformation = s"If you report and pay now, you will still need to declare your capital gains through Self Assessment."
      val whatNextInformation =
        "To report now, you'll need HMRC sign in details. If you do not already have these, you can create them before you sign in."
      val exitSurveyText = "If you do not wish to report capital gains at this time, you can exit now or leave feedback for the CGT calculator service (takes 2 minutes)."
      val exitSurveyLink = "/calculate-your-capital-gains/resident/shares/feedback-survey"
      val exitSurveyLinkText = "leave feedback for the CGT calculator service (takes 2 minutes)"
    }

    object WhatNextNoGain {
      val bulletPointTitle = "If you have made a loss that you would like to use against any future Capital Gains Tax, you can either:"
      val bulletPointOne: String => String = year => s"report it on your Self Assessment return for the $year tax year"
      val bulletPointTwo = "report it now using our online form"
      val importantInformation = s"If you report it now, you will still need to declare your capital gains through Self Assessment."
      val whatNextInformation =
        "To report now, you'll need HMRC sign in details. If you do not already have these, you can create them before you sign in."
      val exitSurveyText = "If you do not wish to report capital gains at this time, you can exit now or leave feedback for the CGT calculator service (takes 2 minutes)."
      val exitSurveyLink = "/calculate-your-capital-gains/resident/shares/feedback-survey"
      val exitSurveyLinkText = "leave feedback for the CGT calculator service (takes 2 minutes)"
    }
  }

  object WhatNextNonSaGain extends Common {
    val title = "What happens next - Calculate your Capital Gains Tax - GOV.UK"
    val heading = "What happens next"
    val reportNow = "Report now"
    val detailsOne = "Use our online form to report and pay your Capital Gains Tax."
    val detailsTwo = "To report now, you'll need HMRC sign in details. If you do not already have these, you can create them before you sign in."
    val exitSurveyText = "If you do not wish to report capital gains at this time, you can exit now or leave feedback for the CGT calculator service (takes 2 minutes)."
    val exitSurveyLink = "/calculate-your-capital-gains/resident/shares/feedback-survey"
    val exitSurveyLinkText = "leave feedback for the CGT calculator service (takes 2 minutes)"
  }

  object WhatNextNonSaLoss extends Common {
    val title = "What happens next - Calculate your Capital Gains Tax - GOV.UK"
    val heading = "What happens next"
    val reportNow = "Report now"
    val govUk = "Return to GOV.UK"
    val detailsOne = "If you have made a loss that you would like to use against any future Capital Gains Tax, you can report it using our online form."
    val detailsTwo = "To report now, you'll need HMRC sign in details. If you do not already have these, you can create them before you sign in."
  }

  object SaUser extends Common {
    val title = "Are you currently in Self Assessment? - Calculate your Capital Gains Tax - GOV.UK"
    val errorTitle = "Error: Are you currently in Self Assessment? - Calculate your Capital Gains Tax - GOV.UK"
    val heading = "Are you currently in Self Assessment?"
    val error = "Select whether you are currently in Self Assessment"
  }
}
