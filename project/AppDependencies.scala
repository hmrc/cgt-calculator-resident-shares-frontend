/*
 * Copyright 2016 HM Revenue & Customs
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

import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion

object AppDependencies {

  val bootstrapVersion         = "5.3.0"
  val govUKTemplateVersion     = "5.66.0-play-27"
  val playUiVersion            = "8.20.0-play-27"
  val playPartialsVersion      = "8.1.0-play-27"
  val httpCachingClientVersion = "9.5.0-play-27"
  val play2PdfVersion          = "1.10.0"
  val jsonJodaVersion          = "2.9.2"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % bootstrapVersion,
    "uk.gov.hmrc" %% "govuk-template" % govUKTemplateVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "it.innove" % "play2-pdf" % play2PdfVersion exclude("com.typesafe.play","*"),
    "com.typesafe.play" %% "play-json-joda" % jsonJodaVersion,
    nettyServer

  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
        "org.mockito" % "mockito-core" % "3.3.3" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
