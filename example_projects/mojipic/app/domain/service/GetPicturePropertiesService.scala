package domain.service

import java.time.LocalDateTime
import javax.inject.Inject
import domain.entity.PictureProperty
import domain.entity.TwitterId
import domain.repository.PicturePropertyRepository
import scala.concurrent.Future

class GetPicturePropertiesService @Inject() (
  picturePropertyRepository: PicturePropertyRepository
) {
  /**
   * 投稿者のTwitter IDと最後に読み込まれた作成日時から画像のプロパティを読み込む
   * @param twitterId 投稿者のTwitter ID
   * @param lastCreatedTime 最後に読み込まれた作成日時
   * @return Future.successful(Seq(PictureProperty)) 読み込みに成功した
   *         Future.failed(DatabaseException)     読み込みに失敗した
   */
  def getAllByTwitterId(twitterId: TwitterId, lastCreatedTime: LocalDateTime): Future[Seq[PictureProperty]] =
    picturePropertyRepository.findAllByTwitterIdAndDateTime(twitterId, lastCreatedTime)

  /**
   * 最後に読み込まれた作成日時から画像のプロパティを読み込む
   * @param lastCreatedTime 最後に読み込まれた作成日時
   * @return Future.successful(Seq(PictureProperty)) 読み込みに成功した
   *         Future.failed(DatabaseException)     読み込みに失敗した
   */
  def getAll(lastCreatedTime: LocalDateTime): Future[Seq[PictureProperty]] =
    picturePropertyRepository.findAllByDateTime(lastCreatedTime)
}
