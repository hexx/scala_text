package controllers

import play.api.mvc.Action
import play.api.mvc.Controller

class HelloController extends Controller {

  val logger = play.api.Logger("hello")

  def get(name: Option[String]) =
    Action { implicit request =>
      logger.info(s"name parameter: $name")

      Ok {
        name
          .map(s => s"Hello, ${s}!")
          .getOrElse("""Please give a name as a query parameter named "name".""")
      }
    }
}
