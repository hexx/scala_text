package controllers

import java.time.LocalDateTime
import javax.inject.Inject
import domain.entity.TwitterId
import domain.exception.DatabaseException
import domain.service.GetPicturePropertiesService
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext

class UsersController @Inject() (
  getPicturePropertiesService: GetPicturePropertiesService,
  executionContext: ExecutionContext
) extends Controller {

  implicit val ec = executionContext

  def getProperties(twitterId: Long, lastCreatedTime: Option[String]) = Action.async {
    val localDateTime = lastCreatedTime.map(LocalDateTime.parse).getOrElse(LocalDateTime.parse("0000-01-01T00:00:00"))
    (for {
      properties <- getPicturePropertiesService.getAllByTwitterId(TwitterId(twitterId), localDateTime)
    } yield {
      Ok(Json.toJson(properties)).as("application/json")
    }).recover {
      case e: DatabaseException => InternalServerError(e.message)
    }
  }
}
