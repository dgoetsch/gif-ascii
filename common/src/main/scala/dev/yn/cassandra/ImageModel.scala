package dev.yn.cassandra

import java.nio.ByteBuffer
import java.util.UUID

import dev.yn.compress.GZIP
import dev.yn.size.ImageSize
import play.api.libs.json.JsValue

case class ImageModel(id: UUID, extraLarge: JsValue, large: JsValue, medium: JsValue, small: JsValue, extraSmall: JsValue) {
  import ImageModel._
  import ImageSize._

  def compress = {
    val compressed = Map(
      EXTRA_LARGE_KEY -> extraLarge,
      LARGE_KEY -> large,
      MEDIUM_KEY -> medium,
      SMALL_KEY -> small,
      EXTRA_SMALL_KEY -> extraSmall
    ).par.map { case(key, json) => key -> compressJson(json) }

    CompressedImageModel(id, compressed(EXTRA_LARGE_KEY), compressed(LARGE_KEY), compressed(MEDIUM_KEY), compressed(SMALL_KEY), compressed(EXTRA_SMALL_KEY))
  }
}

case class CompressedImageModel(id: UUID, extraLarge: ByteBuffer, large: ByteBuffer, medium: ByteBuffer, small: ByteBuffer, extraSmall: ByteBuffer)

object ImageModel {
  def decompress(buffer: ByteBuffer) = GZIP.decompress(buffer.array())
  def compressJson(jsValue: JsValue): ByteBuffer = ByteBuffer.wrap(GZIP.compressString(jsValue.toString))
}
