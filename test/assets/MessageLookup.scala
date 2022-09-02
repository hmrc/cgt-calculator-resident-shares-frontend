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

package assets

object MessageLookup {

  // TO MOVE

  trait Common {

    val externalLink = "(opens in a new window)"
    val change = "Change"
    val back = "Back"
    val continue = "Continue"
    val yes = "Yes"
    val no = "No"
    val day = "Day"
    val month = "Month"
    val year = "Year"

    val readMore = "Read more"

    val mandatoryAmount = "Enter an amount"
    val minimumAmount = "Enter an amount that's £0 or more"
    val maximumAmount = "Enter an amount that's £1,000,000,000 or less"
    val errorRequired = "This field is required"

    def maximumLimit(limit: String): String = s"Enter an amount that's £$limit or less"

    val invalidAmount = "Enter an amount in the correct format e.g. 10000.00"
    val invalidAmountNoDecimal = "Enter an amount in the correct format e.g. 10000"
    val numericPlayErrorOverride = "Enter a number without commas, for example 10000.00"
    val optionReqError = "Choose one of the options"

    val whatToDoNextTextTwo = "You need to tell HMRC about the property"
    val whatToDoNextFurtherDetails = "Further details on how to tell HMRC about this property can be found at"
  }

  object NonResident extends Common {

    val pageHeading = "Calculate your non-resident Capital Gains Tax"
    val errorInvalidDate = "Enter a real date"
    val errorRealNumber = "Enter a number without commas, for example 10000.00"

    object AcquisitionCosts {
      val question = "How much did you pay in costs when you became the property owner?"
      val helpText = "Costs include agent fees, legal fees and surveys"
      val errorNegative = "Enter a positive number for your costs"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your costs"
      val bulletTitle = "This is what you paid for:"
      val bulletOne = "estate agents or auctioneers"
      val bulletTwo = "solicitors or conveyancers, including Stamp Duty Land Tax"
      val bulletThree = "any professional help to value the property, eg surveyor or valuer"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object AcquisitionDate {
      val question = "Do you know the date you signed the contract that made you the owner?"
      val questionTwo = "What was the date you signed the contract that made you the owner?"
    }

    object AcquisitionValue {
      val question = "How much did you pay for the property?"
      val bulletTitle = "Put the market value of the property instead if you:"
      val bulletOne = "inherited it"
      val bulletTwo = "got it as a gift"
      val bulletThree = "bought it from a relative, business partner or someone else you're connected to"
      val bulletFour = "bought it for less than it's worth because the seller wanted to help you"
      val bulletFive = "became the owner before 1 April 1982"
      val bulletLink = "someone else you're connected to"
      val errorNegative = "Enter a positive number for the amount you paid"
      val errorDecimalPlaces = "The amount you paid has too many numbers after the decimal point"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object BoughtForLess {
      val question = "Did you buy the property for less than it was worth because the seller wanted to help you?"
    }

    object CalculationElection {
      val heading = "Working out your total"
      val question = "Which method of calculation would you like?"
      val moreInformation = "How this affects your tax"
      val moreInfoFirstP = "We can work out your tax in a number of ways. Before we give you a final total you have a choice about what to base it on."
      val moreInfoSecondP = "Because the options take different things into account they'll usually give you different totals. " +
        "Some totals may be much larger than others."
      val moreInfoThirdP = "Unless you want to add more tax reliefs, the total you choose is the amount of tax you'll pay."
      val legend = "Tax you'll owe"
      val basedOn = "Based on"
      val linkOne = "Different ways of working out Capital Gains Tax"
      val otherTaxRelief = "Add other tax relief"
      val someOtherTaxRelief = "Other tax relief"
      val timeApportioned = "Working out your total gain, then taxing you on the percentage of it you've made since"
      val rebased = "How much you've gained on the property since"
      val taxStartDate = "5 April 2015"
      val flat = "How much you've gained on the property since you became the owner"
      val whyMore = "Why you might choose a higher total"
      val whyMoreDetailsOne = "You might pick a higher total if you're planning to add more tax reliefs."
      val whyMoreDetailsTwo = "Higher totals sometimes qualify for larger reliefs, which can reduce the amount you owe."
    }

    object CheckYourAnswers {
      val question = "Check your answers"
      val tableHeading = "You've told us"
      val change = "Change"
      val hiddenText = "your response to the question"
    }

    object CurrentIncome {
      val question = "What was your total UK income in the tax year when you stopped owning the property?"
      val linkOne = "Income Tax"
      val linkTwo = "Previous tax years"
      val helpText = "Give a realistic estimate if this was in the current tax year. Include your UK salary before tax, and anything else you pay UK income tax on. Do not include the money you made from selling the property."
      val errorNegative = "Enter a positive number for your current income"
      val errorDecimalPlace = "Your current income has too many numbers after the decimal point"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object CustomerType {
      val question = "Who owned the property?"
      val individual = "I owned it"
      val trustee = "I was a trustee"
      val personalRep = "I was the executor of an estate"
      val errorInvalid = "Invalid customer type"
    }

    object DisabledTrustee {
      val question = "Are you a trustee for someone who's vulnerable?"
      val helpText = "A person's vulnerable if they're disabled, or if they're under 18 and their parents have died"
      val linkOne = "Trusts and Capital Gains Tax"
    }

    object DisposalCosts {

      val question = "How much did you pay in costs when you stopped owning the property?"

      val helpTitle = "This is what you paid for:"
      val helpBulletOne = "estate agents or auctioneers"
      val helpBulletTwo = "solicitors or conveyancers"
      val helpBulletThree = "any professional help to value your property, eg surveyor or valuer"
      val helpBulletFour = "advertising to find a buyer"

      val errorNegativeNumber = "Enter a positive number for your selling costs"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your selling costs"

    }

    object DisposalDate {

      val question = "When did you sign the contract that made someone else the owner?"
      val errorDateAfter = "This can't be before the date you became the owner"

    }

    object SoldForLess {
      val question = "Did you sell the property for less than it was worth to help the buyer?"
    }

    object DisposalValue {

      val question = "How much did you sell the property for?"
      val errorNegativeNumber = "Enter a positive number for the amount you sold the property for"
      val errorDecimalPlaces = "The amount you sold the property for has too many numbers after the decimal point"
      val errorNegative = "Enter a positive number for the amount you sold the property for"
      val bulletIntro = "Put the market value of the property instead if you:"
      val bulletOne = "gave it away as a gift"
      val bulletTwo = "sold it to a relative, business partner or"
      val bulletTwoLink = "someone else you're connected to"
      val bulletThree = "sold it for less than it's worth to help the buyer"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object HowMuchGain {

      val question = "What was your taxable gain?"
      val errorNegativeNumber = "Enter a positive number for the amount on your taxable gain"
      val errorDecimalPlaces = "Taxable Gain for has too many numbers after the decimal point"
    }

    object NoCapitalGainsTax {

      val title = "You have no tax to pay"
      val paragraphOne = "This is because Capital Gains Tax for non-residents only applies to properties which were sold or given away after 5 April 2015."
      val paragraphTwo = "You've told us that you sold or gave away the property on"
      val change = "Change"
      val link = "Capital Gains Tax for non-residents"

    }

    object PreviousLossOrGain {
      val question = "Did your previous properties result in a Capital Gains Tax loss or gain?"
      val mandatoryCheck = "Please tell us whether you made a gain or loss"
      val loss = "Loss"
      val gain = "Gain"
      val neither = "Neither, I broke even"
      val CGTlink = "Capital Gains Tax"
      val previousTaxLink = "Previous tax years"
    }

    object MarketValue {
      val disposalGaveAwayQuestion = "What was the property worth when you gave it away?"
      val disposalSoldQuestion = "What was the market value of the property when you sold it?"

      val disposalHelpText = "You can use a valuation from a surveyor. " +
        "If you don't know the exact value, you must provide a realistic estimate."
      val disposalHelpTextAdditional = "You might have to pay more if we think your estimate is unrealistic."

      val disposalErrorDecimalPlacesGaveAway = "There are too many numbers after the decimal point in your market value" +
        " at the point of giving away"
      val disposalErrorDecimalPlacesSold = "There are too many numbers after the decimal point in your market value" +
        " at the point of being sold"

      val errorNegativeGaveAway = "Enter a positive number for the market value at the point of being given away"
      val errorNegativeSold = "Enter a positive number for the market value at the point of being sold"
    }

    object PersonalAllowance {
      val question = "What was your UK Personal Allowance in the tax year when you stopped owning the property?"
      val link = "Personal Allowances"
      val help = "This the amount of your income that you don't pay tax on. Find out more about"
      val errorNegative = "Enter a positive number for your Personal Allowance"
      val errorDecimalPlaces = "Enter a whole number for your Personal Allowance"
      val errorMaxLimit = "Enter a Personal Allowance that's £"
      val errorMaxLimitEnd = "or less"
    }

    object RebasedCosts {
      val question = "Did you pay to have the property valued at 5 April 2015?"
      val inputQuestion = "How much did it cost to get the property valued?"
      val errorNegative = "Enter a positive number for your costs"
      val errorNoValue = "Enter the value for your costs"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your costs"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object RebasedValue {
      val question = "What was the market value of the property on 5 April 2015?"

      val questionOptionalText = "Only tell us if you owned the property on that date"

      val inputHintText = "If you don't know the exact value, you must provide a realistic estimate. " +
        "You might have to pay more if we think your estimate is unrealistic."
      val additionalContentTitle = "Why we're asking for this"
      val helpHiddenContent = "This value lets us calculate your tax in different ways, which means you may have less tax to pay."

      val errorNoValue = "Enter a value for your property on 5 April 2015"
      val errorNegative = "Enter a positive value for your property on 5 April 2015"
      val errorDecimalPlaces = "The value for your property on 5 April 2015 has too many numbers after the decimal point"


      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object HowBecameOwner {
      val question = "How did you become the owner?"
      val errorMandatory = "Tell us how you became the owner"
      val bought = "Bought it"
      val gifted = "Got it as a gift"
      val inherited = "Inherited it"
    }

    object SoldOrGivenAway {
      val question = "Did you sell or give away the property?"
      val sold = "I sold it"
      val gave = "I gave it away"
    }

    //Acquisition Market Value messages.en
    object AcquisitionMarketValue {
      val errorNegativeNumber = "Enter a positive number for the market value of the property"
      val errorDecimalPlaces = "The market value of the property has too many numbers after the decimal point"
      val hintOne = "You can use a valuation from a surveyor."
      val hintTwo = "If you don't know the exact value, you must provide a realistic estimate. " +
        "You might have to pay more if we think your estimate is unrealistic."
    }

    object WorthBeforeLegislationStart {
      val question = "What was the market value of the property on 31 March 1982?"
      val expandableText = "You only need to pay tax on gains made after this date"
      val expandableTitle = "Why we're asking for this"
    }

    //Worth When Inherited messages.en
    object WorthWhenInherited {
      val question = "What was the market value of the property when you inherited it?"
    }

    //Worth When Gifted To messages.en
    object WorthWhenGiftedTo {
      val question = "What was the market value of the property when you got it as a gift?"
    }

    //Worth When Bought for Less messages.en
    object WorthWhenBoughtForLess {
      val question = "What was the market value of the property when you bought it?"
    }

    object HowMuchLoss {
      val question = "How much loss did you report?"
      val errorNegative = "Enter a positive number for your loss"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your loss"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object BroughtForwardLosses {
      val question = "Do you have losses you want to bring forward from previous tax years?"
      val inputQuestion = "How much would you like to bring forward?"
      val helpTitle = "These are losses on UK properties that:"
      val helpListOne = "are covered by Capital Gains Tax"
      val helpListTwo = "you've declared within 4 years of making them"
      val helpListThree = "you've not already used to reduce the amount of Capital Gains Tax you had to pay"
      val linkOne = "Capital Gains Tax"
      val linkTwo = "Previous tax years"
      val errorDecimalPlaces = "There are too many numbers after the decimal point in your brought forward loss"
      val errorNegative = "Enter a positive number for your brought forward loss"

      def errorMaximum(value: String): String = s"Enter an amount that's £$value or less"
    }

    object Summary {

      val title = "Summary - Calculate your Capital Gains Tax - GOV.UK"
      val secondaryHeading = "You owe"
      val amountOwed = "Amount you owe"
      val calculationDetailsTitle = "Calculation details"
      val totalGain = "Your total gain"
      val totalLoss = "Loss"
      val usedAEA = "Capital Gains Tax allowance used"
      val remainingAEA = "Capital Gains Tax allowance remaining"

      def usedAllowableLosses(taxYear: String): String = s"Loss used from $taxYear tax year"

      def usedBroughtForwardLosses(taxYear: String): String = s"Loss used from tax years before $taxYear"

      val lossesRemaining = "Carried forward loss"
      val taxableGain = "Your taxable gain"
      val taxRate = "Tax rate"
      val prrUSed = "Private resident relief used"
      val personalDetailsTitle = "Personal details"
      val purchaseDetailsTitle = "Owning the property"
      val propertyDetailsTitle = "Property details"
      val salesDetailsTitle = "Selling or giving away the property"
      val deductionsTitle = "Deductions"
      val whatToDoNextText = "What to do next"
      val whatToDoNextContent = "You need to"
      val whatToDoNextLink = "tell HMRC about the property"
      val startAgain = "Start again"
      val calculationElection = "What would you like to base your tax on?"
      val timeCalculation = "The percentage of your total gain you've made since"
      val flatCalculation = "How much you've gained on the property since you became the owner"
      val rebasedCalculation = "How much you've gained on the property since 5 April 2015"
      val lossesCarriedForward = "Loss carried forward"
      val taxYearWarning = "Your total might be less accurate because you didn't sell or give away your property in this tax year"
      val saveAsPdf = "Save as PDF"

      def basedOnYear(year: String): String = s"These figures are based on the tax rates from the $year tax year"
    }
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
        val tableHeading = "You've told us"
        val change = "Change"
        val hiddenText = "your response to the question"
      }

      object SharesSummaryMessages {

        val disposalDateQuestion = "When did you sell or give away the shares?"
        val disposalValueQuestion = "How much did you sell the shares for?"
        val disposalCostsQuestion = "How much did you pay in costs when you sold the shares?"
        val acquisitionValueQuestion = "How much did you pay for the shares?"
        val acquisitionCostsQuestion = "How much did you pay in costs when you got the shares?"

      }

      object ValueBeforeLegislationStart {
        val title = "What were the shares worth on 31 March 1982? - Calculate your Capital Gains Tax - GOV.UK"
        val question = "What were the shares worth on 31 March 1982?"
        val information = "If you had your shares before 31 March 1982, use the market value on 31 March 1982 to work " +
          "out your Capital Gains Tax. After this date, use the original cost."
        val helpText = "Get valuation information from the stock exchange or talk to your stockbroker or fund manager."
        val hintText = "If you owned the shares with someone else, only enter the value of your portion of the shares."

      }

      object DisposalValue {
        val title = "How much did you sell the shares for? - Calculate your Capital Gains Tax - GOV.UK"
        val question = "How much did you sell the shares for?"
        val jointOwnership = "If you owned the shares with someone else, only enter your portion of the sale value."
        val nonValidDate = "Enter a real date Enter a date that is after 6 4 2015"
      }

      //############ Owner Before Legislation Start messages.en #################//
      object OwnerBeforeLegislationStart {
        val title = "Did you own the shares before 1 April 1982? - Calculate your Capital Gains Tax - GOV.UK"
        val heading = "Did you own the shares before 1 April 1982?"
        val errorNoSelect = "Tell us if you owned the shares before 1 April 1982"
      }

      object DidYouInheritThem {
        val question = "Did you inherit the shares?"
        val errorSelect = "Tell us if you inherited the shares"
      }

      //############ Sell For Less messages.en #################//
      object SellForLess {
        val title = "Did you sell the shares for less than they were worth to help the buyer?"
        val newTitle = "Did you sell the shares for less than they were worth to help the buyer? - Calculate your Capital Gains Tax - GOV.UK"
        val errorSelect = s"Tell us if you sold the shares for less than they were worth to help the buyer."
      }

      //############ Worth When Inherited messages.en #################//
      object WorthWhenInherited {
        val title = "What were the shares worth when you inherited them? - Calculate your Capital Gains Tax - GOV.UK"
        val question = "What were the shares worth when you inherited them?"
        val helpText = "Use information from the stock exchange or talk to your stockbroker or fund manager."
        val hintText = "If you owned the shares with someone else, only enter the market value for your portion of the shares."
      }

      //############ Worth When Sold For Less messages.en #################//
      object WorthWhenSoldForLess {
        val title = "What were the shares worth when you sold them? - Calculate your Capital Gains Tax - GOV.UK"
        val question = "What were the shares worth when you sold them?"
        val informationText = "Get information from the stock exchange or talk to your stockbroker or fund manager."
        val jointOwnershipText = "If you owned the shares with someone else, only enter the value of your portion of the shares."
      }

    }

  }


  //########################################################################################

  object IntroductionView {
    val title = "Work out how much Capital Gains Tax you owe"
    val subheading = "Do you need to use this calculator?"
    val paragraph = "You probably don't need to pay Capital Gains Tax if the property you've sold is your own home. You'll be entitled to a tax relief called Private Residence Relief."
    val entitledLinkText = "Find out if you're entitled to Private Residence Relief (opens in a new window)."
    val continuationInstructions = "Continue to use this calculator if you've never lived at the property, or you're entitled to only some or no Private Residence Relief."
  }

  //Disposal Date messages.en
  object DisposalDate {
    val title = "When did you sell or give away the property?"
    val question = "When did you sell or give away the property?"
    val helpText = "For example, 4 9 2016"
    val day = "Day"
    val month = "Month"
    val year = "Year"
    val invalidDayError = "Enter a day"
    val invalidMonthError = "Enter a month"
    val invalidYearError = "Enter a year"
    val realDateError = "Enter a real date"
    val invalidYearRange = "Enter a date in the correct format e.g. 9 12 2015"
    def invalidMinimumDate(minimumDate: String) = s"Enter a date which is after $minimumDate"
  }

  //Outside Tax Years messages.en
  object OutsideTaxYears {
    val title = "The date you've entered isn't supported by this calculator"
    val tooEarly = "You can use this calculator if you've sold a property since 5 April 2015."
    val sharesTooEarly = "You can use this calculator if you've sold shares since 5 April 2015."
    val changeDate = "Change your date"

    def content(year: String): String = s"You can continue to use it, but we'll use the tax rates from the $year tax year."
  }

  //No Tax To Pay messages.en
  object NoTaxToPay {
    val title = "You have no tax to pay"
    val spouseText = "This is because Capital Gains Tax doesn't apply if you give a property to your spouse or civil partner."
    val charityText = "This is because Capital Gains Tax doesn't apply if you give a property to a charity."
  }

  //############ Sell For Less messages.en #################//
  object SellForLess {
    val title = "Did you sell the property for less than it was worth to help the buyer?"
  }

  //############ Worth When Inherited messages.en #################//
  object WorthWhenInherited {
    val title = "What was the property worth when you inherited it?"
    val additionalContent = "You can use a valuation from a surveyor or a property website."
  }

  //############ Worth When Gifted messages.en #################//
  object WorthWhenGifted {
    val question = "What was the property worth when you got it as a gift?"
    val additionalContent = "You can use a valuation from a surveyor or a property website."
  }

  //############ Worth When Bought messages.en #################//
  object WorthWhenBought {
    val question = "What was the property worth when you bought it?"
    val additionalContent = "You can use a valuation from a surveyor or a property website."
  }

  //Disposal Value messages.en
  object DisposalValue {
    val question = "How much did you sell the property for?"
  }

  //Disposal Costs messages.en
  object DisposalCosts {
    val title = "How much did you pay in costs when you stopped owning the property?"
    val pageHeading = "How much did you pay in costs when you stopped owning the property?"
    val helpText = "Costs include agent fees, legal fees and surveys"
  }

  //How Became Owner messages.en
  object HowBecameOwner {
    val title = "How did you become the property owner?"
    val errorMandatory = "Tell us how you became the property owner"
    val bought = "Bought it"
    val gifted = "Got it as a gift"
    val inherited = "Inherited it"
  }

  //############ Bought For Less Than Worth messages.en #################//
  object BoughtForLessThanWorth {
    val title = "Did you buy the property for less than it was worth because the seller wanted to help you?"
  }

  //Acquisition Value messages.en
  object AcquisitionValue {
    val title = "How much did you pay for the property?"
    val pageHeading = "How much did you pay for the property?"
  }

  //Acquisition Costs messages.en
  object AcquisitionCosts {
    val title = "How much did you pay in costs when you became the property owner?"
    val pageHeading = "How much did you pay in costs when you became the property owner?"
    val helpText = "Costs include stamp duty, agent fees, legal fees and surveys"
  }

  //Improvements messages.en


  //Summary messages.en
  object SummaryPage {
    val title = "Summary - Calculate your Capital Gains Tax - GOV.UK"
    val oldTitle = "Summary"
    val pageHeading = "Tax owed"
    val secondaryHeading = "You owe"
    val calcDetailsHeading = "Calculation details"

    def calcDetailsHeadingDate(input: String): String = s"Calculation details for $input tax year"

    val aeaHelp = "You can use this to reduce your tax if you sell something else that's covered by Capital Gains Tax in the same tax year."
    val yourAnswersHeading = "Your answers"
    val totalLoss = "Loss"
    val totalGain = "Total gain"
    val deductions = "Deductions"
    val chargeableLoss = "Carried forward loss"
    val chargeableGain = "Taxable gain"
    val taxRate = "Tax rate"

    def noticeWarning(input: String): String = s"These figures are based on the tax rates from the $input tax year"

    val warning = "Warning"
    val whatToDoNextTitle = "What to do next"
    val whatToDoNextText = "Before you continue, save a copy of your calculation. You will need this when you report your Capital Gains Tax."
    val whatNextYouCan = "You can "
    val whatNextLink = "tell us about this loss "
    val whatNextText = "so that you might need to pay less tax in the future."
    val whatToDoNextTextTwoShares = "You need to tell HMRC about the shares"
    val whatToDoNextNoLossText = "Find out whether you need to"
    val whatToDoNextNoLossLinkProperties = "tell HMRC about the property"
    val whatToDoNextNoLossLinkShares = "tell HMRC about the shares"
    val whatToDoNextLossRemaining = "so that you might need to pay less tax in the future"
    val whatToDoNextSharesLiabilityMessage = "You can tell HMRC about the shares and pay your tax using our online service"
    val whatToDoNextPropertiesLiabilityMessage = "You can tell HMRC about the property and pay your tax using our online service"
    val whatToDoNextLiabilityAdditionalMessage = "You can use the figures on this page to help you do this."

    def aeaRemaining(taxYear: String): String = s"Capital Gains Tax allowance left for $taxYear"

    val saveAsPdf = "Download your Capital Gains Tax calculation (PDF, under 25kB)"

    def remainingAllowableLoss(taxYear: String): String = s"Remaining loss from $taxYear tax year"

    def remainingBroughtForwardLoss(taxYear: String): String = s"Remaining loss from tax years before $taxYear"

    val remainingLossHelp = "You can"
    val remainingLossLink = "use this loss"
    val remainingAllowableLossHelp = "to reduce your Capital Gains Tax if you sell something in the same tax year"
    val remainingBroughtForwardLossHelp = "to reduce your Capital Gains Tax in the future"
    val lettingReliefsUsed = "Letting Relief used"
    val noticeSummary = "Your result may be slightly inaccurate because the calculator does not support the dates you entered. Do not use these figures to report your Capital Gains Tax."

    def deductionsDetailsAllowableLosses(taxYear: String): String = s"Loss from $taxYear tax year"

    val deductionsDetailsCapitalGainsTax = "Capital Gains Tax allowance used"

    def deductionsDetailsLossBeforeYear(taxYear: String): String = s"Loss from tax years before $taxYear"

    def deductionsDetailsAllowableLossesUsed(taxYear: String): String = s"Loss used from $taxYear tax year"

    def deductionsDetailsLossBeforeYearUsed(taxYear: String): String = s"Loss used from tax years before $taxYear"
  }

  //Losses Brought Forward messages.en
  object LossesBroughtForward {
    def title(input: String): String = s"Are you claiming any Capital Gains Tax losses from tax years before $input? - Calculate your Capital Gains Tax - GOV.UK"

    def question(input: String): String = s"Are you claiming any Capital Gains Tax losses from tax years before $input?"

    val helpInfoTitle = "What are Capital Gains Tax losses?"
    val helpInfoSubtitle = "They're losses you've made that:"
    val helpInfoPoint1 = "are covered by Capital Gains Tax"
    val helpInfoPoint2 = "you've declared within 4 years of making the loss"
    val helpInfoPoint3 = "you haven't already used to reduce the amount of Capital Gains Tax you had to pay"
    val helpText = "These are unused losses that are covered by Capital Gains Tax and that you've already reported to HMRC."

    def errorSelect(input: String): String = s"Tell us if you're claiming any Capital Gains Tax losses from tax years before $input"
  }

  //Losses Brought Forward Value messages.en
  object LossesBroughtForwardValue {
    def title(input: String): String = s"What's the total value of your Capital Gains Tax losses from tax years before $input? - Calculate your Capital Gains Tax - GOV.UK"

    def question(input: String): String = s"What's the total value of your Capital Gains Tax losses from tax years before $input?"
  }

  //Current Income messages.en
  object CurrentIncome {
    def title(input: String): String = s"In the $input tax year, what was your income? - Calculate your Capital Gains Tax - GOV.UK"

    def question(input: String): String = s"In the $input tax year, what was your income?"

    val currentYearTitle = "How much do you expect your income to be in this tax year? - Calculate your Capital Gains Tax - GOV.UK"
    val currentYearQuestion = "How much do you expect your income to be in this tax year?"
    val helpText = "Include your salary before tax, and anything else you pay income tax on, but not the money you made from selling the property."
    val helpTextShares = "Include your salary before tax, and anything else you pay income tax on, but not the money you made from selling the shares."
    val linkText = "Income tax"
  }

  //Personal Allowance messages.en
  object PersonalAllowance {
    def question(input: String): String = s"In the $input tax year, what was your Personal Allowance?"
    def title(input: String): String = s"In the $input tax year, what was your Personal Allowance? - Calculate your Capital Gains Tax - GOV.UK"
    val link = "Income tax rates and Personal Allowances"
    val linkText = "Find out more about"
    val help = "This is the amount of your income that you don't pay tax on."
    def listTitle(yearOne: String, yearTwo: String, value: String): String =
      s"In the tax year $yearOne to $yearTwo the UK Personal Allowance was $value unless you:"
    val listOne = "earned more than £100,000"
    val listTwo = "claimed Blind Person's Allowance"
    val inYearQuestion = "How much is your Personal Allowance?"
    val inYearTitle = inYearQuestion + " - Calculate your Capital Gains Tax - GOV.UK"
  }

  //############ Private Residence Relief messages.en #################//
  object PrivateResidenceRelief {
    val title = "Are you entitled to Private Residence Relief?"
    val helpTextOne = "You'll be entitled to Private Residence Relief if you've lived in the property as your main home " +
      "at some point while you owned it. Find out more about"
    val helpTextLink = "Private Residence Relief"
    val errorSelect = "Tell us if you want to claim Private Residence Relief"
  }

  //############ Property Lived In messages.en #################//
  object PropertyLivedIn {
    val title = "Have you ever lived in the property since you became the owner?"
    val errorNoSelect = "Tell us if you have ever lived in the property since you became the owner"
  }

  //############ Shares messages.en ##############//
  object SharesDisposalDate {
    val title = "When did you sell or give away the shares? - Calculate your Capital Gains Tax - GOV.UK"
    val question = "When did you sell or give away the shares?"
  }

  object SharesAcquisitionCosts {
    val title = "How much did you pay in costs when you got the shares? - Calculate your Capital Gains Tax - GOV.UK"
    val question = "How much did you pay in costs when you got the shares?"
    val helpText = "This includes costs for stockbroker fees and Stamp Duty."
    val hintText = "If you owned the shares with someone else, only enter your portion of the costs as agreed with your co-owner."
  }

  object SharesDisposalCosts {
    val title = "How much did you pay in costs when you sold the shares?"
    val newTitle ="How much did you pay in costs when you sold the shares? - Calculate your Capital Gains Tax - GOV.UK"
    val helpText = "This includes costs for stockbroker fees."
    val jointOwnership = "If you owned the shares with someone else, only enter your portion of the costs as agreed with your co-owner."
  }

  object SharesAcquisitionValue {
    val title = "How much did you pay for the shares? - Calculate your Capital Gains Tax - GOV.UK"
    val question = "How much did you pay for the shares?"
    val hintText = "If you owned them with someone else, only enter your share of the purchase."
    val bulletListTitle = "Put the market value of the shares instead if you:"
    val bulletListOne = "inherited them"
    val bulletListTwo = "owned them before 1 April 1982"
  }

  object SharesOtherDisposals {
    val helpOne = "UK residential properties"
    val helpThree = "other shares"
  }

  object PropertiesSellOrGiveAway {
    val title = "Did you sell the property or give it away?"
    val errorMandatory = "Tell us if you sold the property or gave it away"
    val sold = "Sold it"
    val gift = "Gave it away"
  }

  object WhoDidYouGiveItTo {
    val title = "Who did you give the property to?"
    val spouse = "Your spouse or a civil partner"
    val charity = "A charity"
    val other = "Someone else"
    val errormandatory = "Please tell us who you gave the property to"
  }

  object SummaryDetails extends Common {
    val title = "Summary - Calculate your Capital Gains Tax - GOV.UK"
    val oldTitle = "Summary"

    def cgtToPay(taxYear: String): String = s"Capital Gains Tax to pay for the $taxYear tax year"

    val howWeWorkedThisOut = "How we've worked this out"
    val yourTotalGain = "Your total gain"
    val yourTotalLoss = "Your total loss"
    val disposalValue = "Value when you sold the shares"
    val marketValue = "Value when you gave away the shares"
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
    val yourTaxRate = "Your tax rate"

    def taxRate(amount: String, rate: String): String = s"$amount taxable gain multiplied by $rate% tax rate"

    val taxToPay = "Tax to pay"
    val saveAsPdf = "Download your Capital Gains Tax calculation (PDF, under 25kB)"
    val remainingDeductions = "Your remaining deductions"

    def remainingAnnualExemptAmount(taxYear: String): String = s"Annual Exempt Amount left for the $taxYear tax year"

    val lossesToCarryForwardFromCalculation = "Losses you can carry forward from this calculation"
    val broughtForwardLossesRemaining = "Losses to carry forward from previous tax years"
    val whatToDoNext = "What to do next"
    val whatToDoNextDetails = "Before you continue, save a copy of your calculation. You will need this when you report your Capital Gains Tax."
    val whatToDoNextWhatToDoNext = "What to do next"
    val whatToDoNextHeading = "What to do next"
    val whatToDoNextContinue = "Before you continue, save a copy of your calculation. You will need this when you report your Capital Gains Tax."
    val noticeSummary: String = "Your result may be slightly inaccurate because the calculator does not support the dates you entered." +
      " Do not use these figures to report your Capital Gains Tax."
    val ratesHelp = "These rates are based on your Income Tax bands:"

    val bannerPanelTitle = "Help improve HMRC services"
    val bannerPanelLinkURL = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=CGT_Resident_Shares&utm_source=Other&utm_medium=other&t=HMRC&id=144"
    val bannerPanelLinkText = "Sign up to take part in user research (opens in new tab)"
    val bannerPanelCloseVisibleText = "No thanks"

  }

  object WhatNextPages {
    val title = "What to do next"
    val finish = "Return to GOV.UK"
    val reportNow = "Report now"
    val yourOptionsTitle = "Your options"

    object FourTimesAEA {
      val paragraphOne = s"You'll have to report your Capital Gains Tax figures through Self Assessment."
      val paragraphTwo = "You must do this, even though there's no tax to pay."
    }

    object WhatNextGain {
      val bulletPointTitle = "You can either:"
      val bulletPointOne: String => String = year => s"report and pay on your Self Assessment return for the $year tax year"
      val bulletPointTwo = "report and pay now using our online form"
      val importantInformation = s"If you report and pay now, you'll still need to declare your capital gains through Self Assessment."
      val whatNextInformation =
        "To report now you'll need a Government Gateway account. If you don't already have an account, you can get one before you sign in."
      val exitSurveyText = "If you do not wish to report capital gains at this time, you can exit now or leave feedback for the CGT calculator service (takes 2 minutes)."
      val exitSurveyLink = "/calculate-your-capital-gains/resident/shares/feedback-survey"
      val exitSurveyLinkText = "leave feedback for the CGT calculator service (takes 2 minutes)"
    }

    object WhatNextNoGain {
      val bulletPointTitle = "If you've made a loss that you'd like to use against any future Capital Gains Tax, you can either:"
      val bulletPointOne: String => String = year => s"report it on your Self Assessment return for the $year tax year"
      val bulletPointTwo = "report it now using our online form"
      val importantInformation = s"If you report it now, you'll still need to declare your capital gains through Self Assessment."
      val whatNextInformation =
        "To report now you'll need a Government Gateway account. If you don't already have an account, you can get one before you sign in."
      val exitSurveyText = "If you do not wish to report capital gains at this time, you can exit now or leave feedback for the CGT calculator service (takes 2 minutes)."
      val exitSurveyLink = "/calculate-your-capital-gains/resident/shares/feedback-survey"
      val exitSurveyLinkText = "leave feedback for the CGT calculator service (takes 2 minutes)"
    }
  }

  object WhatNextNonSaGain extends Common {
    val title = "What to do next"
    val reportNow = "Report now"
    val govUk = "Return to GOV.UK"
    val detailsOne = "Use our online form to report and pay your Capital Gains Tax."
    val detailsTwo = "To report now you'll need a Government Gateway account. If you don't already have an account, you can get one before you sign in."
    val exitSurveyText = "If you do not wish to report capital gains at this time, you can exit now or leave feedback for the CGT calculator service (takes 2 minutes)."
    val exitSurveyLink = "/calculate-your-capital-gains/resident/shares/feedback-survey"
    val exitSurveyLinkText = "leave feedback for the CGT calculator service (takes 2 minutes)"
  }

  object WhatNextNonSaLoss extends Common {
    val title = "What to do next - Calculate your Capital Gains Tax - GOV.UK"
    val heading = "What to do next"
    val reportNow = "Report now"
    val govUk = "Return to GOV.UK"
    val detailsOne = "If you've made a loss that you'd like to use against any future Capital Gains Tax, you can report it using our online form."
    val detailsTwo = "To report now you'll need a Government Gateway account. If you don't already have an account, you can get one before you sign in."
  }

  object SaUser extends Common {
    val title = "Are you currently in Self Assessment? - Calculate your Capital Gains Tax - GOV.UK"
    val errorTitle = "Error: Are you currently in Self Assessment? - Calculate your Capital Gains Tax - GOV.UK"
    val heading = "Are you currently in Self Assessment?"
    val error = "Select whether you are currently in Self Assessment"
  }
}
