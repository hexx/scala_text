package controllers

import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import com.google.common.io.Files
import com.google.common.net.MediaType
import domain.entity.PictureId
import domain.entity.PictureProperty
import domain.entity.TwitterId
import domain.exception.ConversionFailureException
import domain.exception.ConvertingException
import domain.exception.DatabaseException
import domain.exception.InvalidContentTypeException
import domain.exception.PictureNotFoundException
import domain.service.GetPictureService
import domain.service.PostPictureService
import play.api.cache.CacheApi
import play.api.libs.Files.TemporaryFile
import play.api.mvc.Action
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PicturesController @Inject() (
  postPictureService: PostPictureService,
  getPictureService: GetPictureService,
  clock: Clock,
  executionContext: ExecutionContext,
  val cache: CacheApi
) extends TwitterLoginController {

  implicit val ec = executionContext

  private[this] def createPictureProperty(
    twitterId: TwitterId,
    file: FilePart[TemporaryFile],
    form: MultipartFormData[TemporaryFile]
  ): PictureProperty.Value = {
    val overlayText = form.dataParts.get("overlaytext").flatMap(_.headOption).getOrElse("")
    val overlayTextSize = form.dataParts.get("overlaytextsize").flatMap(_.headOption).getOrElse("60").toInt
    val contentType = MediaType.parse(file.contentType.getOrElse("application/octet-stream"))
    PictureProperty.Value(
      PictureProperty.Status.Converting,
      twitterId,
      file.filename,
      contentType,
      overlayText,
      overlayTextSize,
      LocalDateTime.now(clock))
  }

  def post = TwitterLoginAction.async { request =>
    (request.accessToken, request.body.asMultipartFormData) match {
      case (Some(accessToken), Some(form)) =>
        form.file("file") match {
          case Some(file) =>
            val property = createPictureProperty(TwitterId(accessToken.getUserId), file, form)
            postPictureService
              .post(Files.toByteArray(file.ref.file), property)
              .map(_ => Ok)
              .recover {
              case e: InvalidContentTypeException => BadRequest(e.message)
              case e: DatabaseException => InternalServerError(e.message)
              case e: ConversionFailureException => InternalServerError(e.message)
            }
          case None =>
            Future.successful(BadRequest("File parameter is not found"))
        }
      case (None, _) =>
        Future.successful(Unauthorized("Need to login by Twitter to post a picture"))
      case _ =>
        Future.successful(BadRequest("Body is not found"))
    }
  }

  def get(pictureId: Long) = Action.async { request =>
    (for {
      (converted, property) <- getPictureService.get(PictureId(pictureId))
    } yield Ok(converted.binary).as(property.value.contentType.toString)).recover {
      case e: PictureNotFoundException => NotFound(e.message)
      case e: ConversionFailureException => BadRequest(e.message)
      case e: ConvertingException => BadRequest(e.message)
    }
  }
}
