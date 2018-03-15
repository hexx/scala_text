package domain.repository

import domain.entity.ConvertedPicture
import domain.entity.PictureId
import scala.concurrent.Future

trait ConvertedPictureRepository {
  /**
   * 変換後の画像を保存する
   * @param converted 変換後の画像
   * @return Future.successful(())               保存に成功した
   *         Future.failed(DatabaseException) 保存に失敗した
   */
  def create(converted: ConvertedPicture): Future[Unit]

  /**
   * 変換後の画像を読み込む
   * @param pictureId 画像ID
   * @return Future.successful(ConvertedPicture) 読み込みに成功した
   *         Future.failed(DatabaseException) 読み込みに失敗した
   */
  def find(pictureId: PictureId): Future[ConvertedPicture]
}
