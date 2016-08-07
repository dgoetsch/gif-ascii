package frame

import java.awt.image.BufferedImage
import play.api.Logger

class ImageToAsciiConverter

object ImageToAsciiConverter {
  import ImageChunkUtils._
  import frame.Types._

  val Log = Logger(classOf[ImageToAsciiConverter])

  case class ProcessFrameRequest(frame: BufferedImage, size: Int)

  val xtraModifiers = Seq("xtra", "supe", "more")

  def toAscii(request: ProcessFrameRequest): AsciiFrame = {
    val height: Int = request.frame.getHeight()
    val width: Int = request.frame.getWidth()
    val heightMultiplier = math.max(1, height / request.size)
    val widthMultiplier = math.max(1, heightMultiplier / 3 * 2)

    Log.debug(s"processing: $height, $width, $heightMultiplier, $widthMultiplier")

    doWithChunks[String, String](
      ChunkScaleData(widthMultiplier, heightMultiplier, height, width),
      { (widthScale, heightScale) =>
        calculateColor(request.frame, widthScale, heightScale)
      },
      { line => line.mkString }
    )
  }

  def calculateColor(
                      frame: BufferedImage,
                      width: DimensionScale,
                      height: DimensionScale): String = {
    val colorSeq = doWithEachCoordinateInChunk(frame,
      width,
      height,
      { (curX, curY) =>
        val rgb: Int = frame.getRGB(curX, curY)
        (rgb & 0xFF0000 >> 16, rgb & 0x00FF00 >> 8, rgb & 0x0000FF >> 0)
      })

    colorSeq.foldLeft(0) { (oldColor, newColors) =>
      oldColor + newColors._1 + newColors._2 + newColors._3
    } / (colorSeq.length * 3) match {
      case c if c < 30 => "@"
      case c if c < 60 => "%"
      case c if c < 90 => "#"
      case c if c < 120 => "*"
      case c if c < 150 => "+"
      case c if c < 180 => "="
      case c if c < 210 => "-"
      case c if c < 240 => ":"
      case c if c < 270 => "."
      case _ => " "
    }
  }
}

