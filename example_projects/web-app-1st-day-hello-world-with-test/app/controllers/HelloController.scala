package controllers

import play.api.mvc.Action
import play.api.mvc.Controller

class HelloController extends Controller {

  def get(name: Option[String]) =
    Action { implicit request =>
      Ok {
        name
          .map(s => s"Hello, ${s}!")
          .getOrElse("""Please give a name as a query parameter named "name".""")
      }
    }
}
