package domain.entity

/**
 * 変換後の画像
 * @param id 画像ID
 * @param binary 変換後の画像のバイナリデータ
 */
case class ConvertedPicture(id: PictureId, binary: Array[Byte])
