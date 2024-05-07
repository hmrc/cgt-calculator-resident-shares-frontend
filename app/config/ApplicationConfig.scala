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

package config

import play.api.Environment
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

trait AppConfig {
  val assetsPrefix: String
  val contactFormServiceIdentifier: String
  val contactFrontendPartialBaseUrl: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val residentIFormUrl: String
  val urBannerLink: String
  val feedbackSurvey: String
  val isWelshEnabled: Boolean
}

class ApplicationConfig @Inject()(environment: Environment, val servicesConfig: ServicesConfig) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  private lazy val contactFrontendService = servicesConfig.baseUrl("contact-frontend")
  lazy val contactHost = servicesConfig.getConfString("contact-frontend.www", "")

  lazy val assetsPrefix = loadConfig(s"assets.url") + loadConfig(s"assets.version")

  val contactFormServiceIdentifier = "CGT"
  lazy val contactFrontendPartialBaseUrl = s"$contactFrontendService"
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val feedbackSurvey: String = loadConfig(s"feedback-frontend.url")


  lazy val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=CGT_Resident_Shares&utm_source=Other&utm_medium=other&t=HMRC&id=144"

  lazy val residentIFormUrl: String = loadConfig(s"resident-iForm.url")
  lazy val baseUrl = servicesConfig.baseUrl("capital-gains-calculator")

  def userResearchBannerEnabled: Boolean = servicesConfig.getBoolean(("user-research-banner.enabled"))

  lazy val isWelshEnabled: Boolean = servicesConfig.getBoolean("features.welsh-translation")
}

