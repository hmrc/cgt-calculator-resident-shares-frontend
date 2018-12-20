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

package config

import akka.actor.ActorSystem
import play.api.Mode.Mode
import play.api.{Configuration, Play}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.http.{HttpDelete, HttpGet, HttpPost, HttpPut}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}

trait WiringConfig {
  def appNameConfiguration: Configuration = Play.current.configuration
  def runModeConfiguration: Configuration = Play.current.configuration
  def mode: Mode = Play.current.mode
}

object FrontendAuditConnector extends Auditing with AppName with WiringConfig {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

trait Hooks extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override lazy val auditConnector: Auditing = FrontendAuditConnector
}

trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete with Hooks with AppName
object WSHttp extends WSHttp with WiringConfig {
  override val configuration = Some(appNameConfiguration.underlying)

  override def actorSystem : ActorSystem = Play.current.actorSystem
}

object FrontendAuthConnector extends AuthConnector with ServicesConfig with WiringConfig {
  val serviceUrl = baseUrl("auth")
  lazy val http = WSHttp
}

object CalculatorSessionCache extends SessionCache with ServicesConfig with AppName with WiringConfig {
  override lazy val domain = getConfString("cachable.session-cache.domain",
    throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
  override lazy val baseUri = baseUrl("cachable.session-cache")
  override lazy val defaultSource = appName
  override lazy val http = WSHttp
}
