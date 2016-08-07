package actors

import java.io.InputStream
import javax.imageio.{ImageIO, ImageReader}
import frame.ImageToAsciiConverter
import ImageToAsciiConverter.ProcessFrameRequest
import actors.GifReader.{TransformResult, TransformRequest}
import akka.actor.{ActorLogging, Actor}
import javax.inject.Singleton
import com.sun.imageio.plugins.gif.{GIFImageReaderSpi, GIFImageReader}
import play.api.Logger
import request.ImageSize

@Singleton
class GifReader extends Actor with ActorLogging {
  val Log = Logger(classOf[GifReader])

  override def receive: Receive = {
    case req : TransformRequest => {
      val ir: ImageReader = new GIFImageReader(new GIFImageReaderSpi)
      ir.setInput(ImageIO.createImageInputStream(req.inputStream))

      //Do NOT read frame in parallel
      val frames = (0 until ir.getNumImages(true))
        .map{ frameIndex => ir.read(frameIndex) }

      //perform transformations in parallel
      val resultMap = ImageSize.sizeMap.par.map { case(sizeKey, size) =>
        sizeKey -> frames.par.map { frame => ImageToAsciiConverter.toAscii(ProcessFrameRequest(frame, size))}.seq
      }.seq

      sender() ! TransformResult(resultMap)
    }
  }
}

object GifReader extends NamedActor {
  import frame.Types._
  case class TransformRequest(uri: String, inputStream: InputStream, targetSize: Option[String])
  case class TransformResult(framesBySize: Map[String, AsciiGif])

  override def name: String = "gifReader"
}
