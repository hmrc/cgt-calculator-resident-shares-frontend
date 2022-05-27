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

package connectors

import config.ApplicationConfig
import javax.inject.Inject
import play.api.libs.json.Format
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionCacheConnector @Inject()(val http: DefaultHttpClient,
                                      calculatorConnector: CalculatorConnector,
                                      appConfig: ApplicationConfig) extends SessionCache {

  override lazy val domain = appConfig.servicesConfig.getConfString("cachable.session-cache.domain",
    throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
  override lazy val baseUri = appConfig.servicesConfig.baseUrl("cachable.session-cache")

  override lazy val defaultSource: String = "cgt-calculator-resident-shares-frontend"

  def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[CacheMap] = {
    cache(key, data)
  }

  def fetchAndGetFormData[T](key: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {

    fetchAndGetEntry(key)
  }

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    remove()
  }
}
