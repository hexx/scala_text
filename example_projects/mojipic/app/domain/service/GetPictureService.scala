package domain.service

import javax.inject.Inject
import domain.entity.ConvertedPicture
import domain.entity.PictureId
import domain.entity.PictureProperty
import domain.exception.ConversionFailureException
import domain.exception.ConvertingException
import domain.repository.ConvertedPictureRepository
import domain.repository.PicturePropertyRepository
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class GetPictureService @Inject() (
  convertedPictureRepository: ConvertedPictureRepository,
  picturePropertyRepository: PicturePropertyRepository,
  executionContext: ExecutionContext
) {

  implicit val ec = executionContext

  /**
   * 画像IDから変換後の画像と画像のプロパティを取得する
   * @param pictureId 画像ID
   * @return Future.successful((ConvertedPicture, PictureProperty)) 変換後の画像と画像のプロパティ
   *         Future.failed(PictureNotFoundException)                画像IDの画像が存在しない
   *         Future.failed(ConversionFailureException)              画像の変換に失敗した
   *         Future.failed(ConvertingException)                     画像の変換中
   *         Future.failed(DatabaseException)                       データベースからの読み込みに失敗した
   */
  def get(pictureId: PictureId): Future[(ConvertedPicture, PictureProperty)] = {
    for {
      property <- picturePropertyRepository.find(pictureId)
      picture <- property.value.status match {
        case PictureProperty.Status.Success =>
          convertedPictureRepository.find(pictureId)
        case PictureProperty.Status.Failure =>
          Future.failed(new ConversionFailureException(s"Picture conversion is failed. Picture Id: ${pictureId.value}"))
        case PictureProperty.Status.Converting =>
          Future.failed(new ConvertingException(s"Picture is converting. Picture Id: ${pictureId.value}"))
      }
    } yield (picture, property)
  }
}
