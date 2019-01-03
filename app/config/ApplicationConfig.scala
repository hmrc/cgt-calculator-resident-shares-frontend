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

package config

import play.api.{Configuration, Play}
import play.api.Mode.Mode
import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val assetsPrefix: String
  val analyticsToken: String
  val analyticsHost: String
  val contactFormServiceIdentifier: String
  val contactFrontendPartialBaseUrl: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val residentIFormUrl: String
  val urBannerLink: String
  val feedbackSurvey: String
}

object ApplicationConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))
  private def getFeature(key: String) = configuration.getBoolean(key).getOrElse(false)

  private val contactFrontendService = baseUrl("contact-frontend")
  private val contactHost = configuration.getString(s"$env.microservice.services.contact-frontend.host").getOrElse("")

  override lazy val assetsPrefix = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  override lazy val analyticsToken = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"google-analytics.host")

  override val contactFormServiceIdentifier = "CGT"
  override lazy val contactFrontendPartialBaseUrl = s"$contactFrontendService"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override val feedbackSurvey: String = loadConfig(s"feedback-survey-frontend.url")


  override lazy val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=CGT_Resident_Shares&utm_source=Other&utm_medium=other&t=HMRC&id=144"

  override val residentIFormUrl: String = loadConfig(s"resident-iForm.url")

  override protected def mode: Mode = Play.current.mode
  override protected def runModeConfiguration: Configuration = Play.current.configuration
}
