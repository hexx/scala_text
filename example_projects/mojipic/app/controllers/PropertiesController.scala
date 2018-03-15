package controllers

import java.time.LocalDateTime
import javax.inject.Inject
import domain.exception.DatabaseException
import domain.service.GetPicturePropertiesService
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext

class PropertiesController @Inject() (
  getPicturePropertiesService: GetPicturePropertiesService,
  executionContext: ExecutionContext
) extends Controller {

  implicit val ec = executionContext

  def getAll(lastCreatedDate: Option[String]) = Action.async {
    val localDateTime = lastCreatedDate.map(LocalDateTime.parse).getOrElse(LocalDateTime.parse("0000-01-01T00:00:00"))
    (for {
      properties <- getPicturePropertiesService.getAll(localDateTime)
    } yield {
      Ok(Json.toJson(properties)).as("application/json")
    }).recover {
      case e: DatabaseException => InternalServerError(e.message)
    }
  }
}
