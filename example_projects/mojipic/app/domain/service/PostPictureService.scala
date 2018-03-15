package domain.service

import javax.inject.Inject
import com.google.common.net.MediaType
import domain.entity.OriginalPicture
import domain.entity.PictureProperty
import domain.exception.ConversionFailureException
import domain.exception.InvalidContentTypeException
import domain.repository.PicturePropertyRepository
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PostPictureService @Inject() (
  convertPictureService: ConvertPictureService,
  picturePropertyRepository: PicturePropertyRepository,
  executionContext: ExecutionContext
) {
  val availableMediaTypes = Seq(MediaType.JPEG, MediaType.PNG, MediaType.GIF, MediaType.BMP)

  implicit val ec = executionContext

  /**
   * 投稿された画像を受け取り、画像のプロパティを保存し、画像の変換を開始する
   * @param binary 投稿された画像
   * @param property 投稿された画像のプロパティ
   * @return Future.successful(())                      画像を受け取り、変換を開始した
   *         Future.failed(InvalidContentTypeException) 投稿された画像のContent-Typeが受け付けられないものだった
   *         Future.failed(DatabaseException)           データベースへの保存に失敗した
   *         Future.failed(ConversionFailureException)  画像の変換に失敗した
   */
  def post(binary: Array[Byte], property: PictureProperty.Value): Future[Unit] = {
    if (availableMediaTypes.contains(property.contentType)) {
      for {
        id <- picturePropertyRepository.create(property)
        _ <- convertPictureService.convert(OriginalPicture(id, binary)).recoverWith {
          case e: ConversionFailureException =>
            picturePropertyRepository
              .updateStatus(id, PictureProperty.Status.Failure)
              .flatMap(_ => Future.failed(e))
        }
      } yield ()
    } else {
      Future.failed(InvalidContentTypeException(s"Invalid content type: ${property.contentType}"))
    }
  }
}
