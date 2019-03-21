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

package controllers

import config.ApplicationConfig
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

class CgtLanguageController @Inject()(mcc: MessagesControllerComponents, appConfig: ApplicationConfig)
extends FrontendController(mcc) with I18nSupport {

  /** Converts a string to a URL, using the route to this controller. **/
  def langToCall(lang: String): Call = controllers.routes.CgtLanguageController.switchToLanguage(lang)

  /** Provides a fallback URL if there is no referer in the request header. **/
  def fallbackURL: String = "/"

  /** Returns a mapping between strings and the corresponding Lang object. **/
  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def lang: Lang =  mcc.langs.preferred(languageMap.values.toSeq)
  def getMessage(s: String): String =  mcc.messagesApi(s)(lang)

  def switchToLanguage(language: String): Action[AnyContent] = Action { implicit request =>
    val enabled = isWelshEnabled
    val lang =
      if (enabled) languageMap.getOrElse(language, LanguageUtils.getCurrentLang)
      else Lang("en")
    val redirectURL = request.headers.get(REFERER).getOrElse(fallbackURL)

    Redirect(redirectURL).withLang(Lang.apply(lang.code)).flashing(LanguageUtils.FlashWithSwitchIndicator)
  }

  private def isWelshEnabled = {
    appConfig.servicesConfig.getBoolean("microservice.services.features.welsh-translation")
  }
}
