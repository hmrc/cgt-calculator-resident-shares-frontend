/*
 * Copyright 2023 HM Revenue & Customs
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

package repositories

import play.api.Configuration
import play.api.mvc.Request
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.cache.SessionCacheRepository
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class SessionRepository @Inject()(
                                   mongoComponent: MongoComponent,
                                   config: Configuration,
                                   timestampSupport: TimestampSupport
                                 )(implicit ec: ExecutionContext)
  extends SessionCacheRepository(
    mongoComponent = mongoComponent,
    collectionName = config.get[String]("appName"),
    ttl = Duration(config.get[Int]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS),
    timestampSupport = timestampSupport,
    sessionIdKey = SessionKeys.sessionId
  ) {

  def clear(implicit request: Request[_]): Future[Unit] = {
    cacheRepo.deleteEntity(request)
  }

}