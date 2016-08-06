package frame

import java.awt.image.BufferedImage

object ImageChunkUtils {
  case class ChunkScaleData(widthMultiplier: Int, heightMultiplier: Int, height: Int, width: Int)
  case class DimensionScale(chunkSize: Int, index: Int, max: Int)

  def doWithChunks[T](scale: ChunkScaleData, f: (DimensionScale, DimensionScale) => T): Seq[Seq[T]] = {
    doWithChunks[T, Seq[T]](scale, f, line => line)
  }

  def doWithChunks[T, R](scale: ChunkScaleData, f: (DimensionScale, DimensionScale) => T, eachLine: (Seq[T]) => R): Seq[R] = {
    (0 until scale.height by scale.heightMultiplier).map { y =>
      eachLine((0 until scale.width by scale.widthMultiplier).map { x =>
        f(DimensionScale(scale.widthMultiplier, x, scale.width), DimensionScale(scale.heightMultiplier, y, scale.height))
      })
    }
  }

  /**
    * perform an operation on all of the coordinates within a chunk
    *
    * @param fream
    * @param width
    * @param height
    * @param f
    * @tparam T
    * @return
    */
  def doWithEachCoordinateInChunk[T](fream: BufferedImage,
                                     width: DimensionScale,
                                     height: DimensionScale, f: (Int, Int) => T): Seq[T]  = {
    (width.index until math.min(width.max, width.index + width.chunkSize)).flatMap { curX =>
      (height.index until math.min(height.max, height.index + height.chunkSize)).map { curY =>
        f(curX, curY)
      }
    }
  }
}
