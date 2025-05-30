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

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

trait AppConfig {
  lazy val residentIFormUrl: String
  lazy val urBannerLink: String
  lazy val feedbackSurvey: String
  lazy val isWelshEnabled: Boolean
}

class ApplicationConfig @Inject()(val servicesConfig: ServicesConfig) extends AppConfig {
  private def loadConfig(key: String) = servicesConfig.getString(key)

  lazy val feedbackSurvey: String = loadConfig(s"feedback-frontend.url")

  private lazy val basGatewayUrl: String = loadConfig(s"bas-gateway-frontend.host")

  private val signOutUri: String = loadConfig("sign-out.uri")

  lazy val signOutUrl: String = s"$basGatewayUrl$signOutUri"

  lazy val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=CGT_Resident_Shares&utm_source=Other&utm_medium=other&t=HMRC&id=144"

  lazy val residentIFormUrl: String = loadConfig(s"resident-iForm.url")
  lazy val baseUrl: String = servicesConfig.baseUrl("capital-gains-calculator")

  def userResearchBannerEnabled: Boolean = servicesConfig.getBoolean(("user-research-banner.enabled"))

  lazy val isWelshEnabled: Boolean = servicesConfig.getBoolean("features.welsh-translation")
}

