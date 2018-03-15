package domain.entity

/**
 * 投稿された画像
 * @param id 画像ID
 * @param binary 投稿された画像のバイナリデータ
 */
case class OriginalPicture(id: PictureId, binary: Array[Byte])
