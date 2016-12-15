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

package controllers.utils

import java.net.URLEncoder

import play.api.Logger
import play.api.http.{Status => HttpStatus}
import play.api.mvc.{Action, AnyContent, Request, RequestHeader}
import play.twirl.api.Html
import config.{AppConfig, ApplicationConfig, WSHttp}
import views.html.feedback.feedback_thankyou
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, _}
import uk.gov.hmrc.play.partials._

import scala.concurrent.Future

object FeedbackController extends FeedbackController with PartialRetriever {

  override val httpPost = WSHttp
  override val httpGet = WSHttp

  override def contactFormReferer(implicit request: Request[AnyContent]): String = request.headers.get(REFERER).getOrElse("")
  override def localSubmitUrl(implicit request: Request[AnyContent]): String = routes.FeedbackController.submit().url

  protected def authConnector: AuthConnector = config.FrontendAuthConnector
  protected def loadPartial(url : String)(implicit request : RequestHeader) : HtmlPartial = ???

  implicit val cachedStaticHtmlPartialRetriever: CachedStaticHtmlPartialRetriever = new CachedStaticHtmlPartialRetriever {
    override val httpGet: HttpGet = WSHttp
  }

  override implicit val formPartialRetriever: FormPartialRetriever = new FormPartialRetriever {
    override def httpGet: HttpGet = WSHttp
    override def crypto: (String) => String = cookie => SessionCookieCryptoFilter.encrypt(cookie)
  }

  override val applicationConfig: AppConfig = ApplicationConfig
}

trait FeedbackController extends FrontendController with Actions {
  implicit val formPartialRetriever: FormPartialRetriever
  implicit val cachedStaticHtmlPartialRetriever: CachedStaticHtmlPartialRetriever

  def httpPost: HttpPost
  def contactFormReferer(implicit request: Request[AnyContent]): String
  def localSubmitUrl(implicit request: Request[AnyContent]): String

  val applicationConfig: AppConfig

  private val TICKET_ID = "ticketId"
  private def feedbackFormPartialUrl(implicit request: Request[AnyContent]) =
    s"${applicationConfig.contactFrontendPartialBaseUrl}/contact/beta-feedback/form/?submitUrl=${urlEncode(localSubmitUrl)}" +
      s"&service=${urlEncode(applicationConfig.contactFormServiceIdentifier)}&referer=${urlEncode(contactFormReferer)}"
  private def feedbackHmrcSubmitPartialUrl(implicit request: Request[AnyContent]) =
    s"${applicationConfig.contactFrontendPartialBaseUrl}/contact/beta-feedback/form?resubmitUrl=${urlEncode(localSubmitUrl)}"
  private def feedbackThankYouPartialUrl(ticketId: String)(implicit request: Request[AnyContent]) =
    s"${applicationConfig.contactFrontendPartialBaseUrl}/contact/beta-feedback/form/confirmation?ticketId=${urlEncode(ticketId)}"

  def show: Action[AnyContent] = UnauthorisedAction {
    implicit request =>
      (request.session.get(REFERER), request.headers.get(REFERER)) match {
        case (None, Some(ref)) => Ok(views.html.feedback.feedback(feedbackFormPartialUrl, None)).withSession(request.session + (REFERER -> ref))
        case _ => Ok(views.html.feedback.feedback(feedbackFormPartialUrl, None))
      }
  }

  def submit: Action[AnyContent] = UnauthorisedAction.async {
    implicit request =>
      request.body.asFormUrlEncoded.map { formData =>
        httpPost.POSTForm[HttpResponse](feedbackHmrcSubmitPartialUrl, formData)(rds = readPartialsForm, hc = partialsReadyHeaderCarrier).map {
          resp =>
            resp.status match {
              case HttpStatus.OK => Redirect(routes.FeedbackController.thankyou()).withSession(request.session + (TICKET_ID -> resp.body))
              case HttpStatus.BAD_REQUEST => BadRequest(views.html.feedback.feedback(feedbackFormPartialUrl, Some(Html(resp.body))))
              case status => Logger.warn(s"Unexpected status code from feedback form: $status"); InternalServerError
            }
        }
      }.getOrElse {
        Logger.warn("Trying to submit an empty feedback form")
        Future.successful(InternalServerError)
      }
  }

  def thankyou: Action[AnyContent] = UnauthorisedAction {
    implicit request =>
      val ticketId = request.session.get(TICKET_ID).getOrElse("N/A")
      val referer = request.session.get(REFERER).getOrElse("/")
      Ok(feedback_thankyou(feedbackThankYouPartialUrl(ticketId), referer)).withSession(request.session - REFERER)
  }

  private def urlEncode(value: String) = URLEncoder.encode(value, "UTF-8")

  private def partialsReadyHeaderCarrier(implicit request: Request[_]): HeaderCarrier = {
    val hc1 = CgtHeaderCarrierForPartialsConverter.headerCarrierEncryptingSessionCookieFromRequest(request)
    CgtHeaderCarrierForPartialsConverter.headerCarrierForPartialsToHeaderCarrier(hc1)
  }

  object CgtHeaderCarrierForPartialsConverter extends HeaderCarrierForPartialsConverter {
    override val crypto = encryptCookieString _

    def encryptCookieString(cookie: String) : String = {
      SessionCookieCryptoFilter.encrypt(cookie)
    }
  }

  implicit val readPartialsForm: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }
}
