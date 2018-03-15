package domain.service

import domain.entity.OriginalPicture
import scala.concurrent.Future

trait ConvertPictureService {
  /**
   * 投稿された画像の変換を開始する
   * 変換は非同期に実行され、結果はConvertedPictureRepositoryに保存される
   * @param original 投稿された画像
   * @return Future.successful(())                     変換を開始した
   *         Future.failed(ConversionFailureException) 変換を開始できなかった
   */
  def convert(original: OriginalPicture): Future[Unit]
}
