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

package connectors

import config.CalculatorSessionCache
import play.api.libs.json.Format
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SessionCacheConnector extends SessionCacheConnector {
  override lazy val sessionCache: SessionCache = CalculatorSessionCache
}

trait SessionCacheConnector {

  val sessionCache: SessionCache

  def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[CacheMap] = {
    sessionCache.cache(key, data)
  }

  def fetchAndGetFormData[T](key: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry(key)
  }

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }
}
