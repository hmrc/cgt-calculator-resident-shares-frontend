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

import play.core.PlayVersion
import sbt.*

object AppDependencies {

  lazy val bootstrapVersion         = "7.22.0"
  lazy val playVersion              = "play-28"
  lazy val jsonJodaVersion          = "2.9.4"
  lazy val taxYearVersion           = "3.3.0"
  lazy val hmrcMongoVersion         = "1.3.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion"         % "8.5.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "tax-year"                   % taxYearVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq[ModuleID](
        "uk.gov.hmrc" %% s"bootstrap-test-$playVersion" % bootstrapVersion,
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0",
        "org.scalatestplus" %%  "scalatestplus-mockito" % "1.0.0-M2",
        "org.mockito" % "mockito-core" % "3.12.4",
        "org.jsoup" % "jsoup" % "1.15.4",
        "com.typesafe.play" %% "play-test" % PlayVersion.current,
        "uk.gov.hmrc.mongo" %%  s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion
      ).map(_ % scope)
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
