package controllers

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.Controller

class HelloController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def get(name: Option[String]) =
    Action { implicit request =>
      Ok {
        name
          .map(s => Messages("hello", s))
          .getOrElse(Messages("noQuery"))
      }
    }
}
