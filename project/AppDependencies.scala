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

import sbt.*

object AppDependencies {
  lazy val bootstrapVersion = "9.7.0"
  lazy val playVersion = "play-30"
  lazy val taxYearVersion = "5.0.0"
  lazy val hmrcMongoVersion = "2.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc-$playVersion" % "8.5.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion" % hmrcMongoVersion,
    "uk.gov.hmrc" %% "tax-year" % taxYearVersion
  )

  def test(scope: String = "test"): Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc" %% s"bootstrap-test-$playVersion" % bootstrapVersion % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % scope,
    "org.scalatestplus" %% "scalatestplus-mockito" % "1.0.0-M2" % scope,
    "org.mockito" % "mockito-core" % "5.15.2" % scope,
    "org.jsoup" % "jsoup" % "1.18.3" % scope,
    "org.playframework" %% "play-test" % playVersion % scope,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion % scope,
    "com.github.tomakehurst" % "wiremock" % "3.0.0-beta-7" % scope
  )
}
