package service

import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Singleton, Named, Inject}

import actors.GifReader.{TransformRequest, TransformResult}
import akka.actor.ActorRef
import akka.util.Timeout
import cassandra.AsciiGifWriteCQL
import controllers.DownloadRequest
import dev.yn.cassandra.{UrlKeyModel, ImageModel}
import dev.yn.size.ImageSize
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import repository.AsciiImageRepository
import akka.pattern.ask

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class ImageInjestService @Inject()(@Named("gifReader") gifReader: ActorRef, gifDownloadService: NetworkDataService, configuration: Configuration) {
  import frame.Types._
  implicit val timeout = Timeout(30000, TimeUnit.MILLISECONDS)
  val Log = Logger(classOf[ImageInjestService])

  import ImageSize._

  lazy val repository = new AsciiImageRepository(configuration)

  def injest(downloadRequest: DownloadRequest): Future[AsciiGif] = {
    getAscii(downloadRequest).flatMap {
      case Some(previouslySaved) =>
        Log.info(s"fetched already existing image $downloadRequest")
        Future.successful(previouslySaved)
      case None =>
        Log.info(s"saving new image $downloadRequest")
        val id = UUID.randomUUID()
        for {
          transformResult <- downloadAndTransform(downloadRequest);
          cassandraResultSet <- repository.insertGif(ImageModel(id,
            Json.toJson(transformResult.framesBySize(EXTRA_LARGE_KEY)),
            Json.toJson(transformResult.framesBySize(LARGE_KEY)),
            Json.toJson(transformResult.framesBySize(MEDIUM_KEY)),
            Json.toJson(transformResult.framesBySize(SMALL_KEY)),
            Json.toJson(transformResult.framesBySize(EXTRA_SMALL_KEY))), UrlKeyModel(downloadRequest.url, id))
        } yield transformResult.framesBySize(downloadRequest.size.map(_.targetSizeKey).getOrElse(MEDIUM_KEY))
    }
  }

  def getAscii(downloadRequest: DownloadRequest): Future[Option[AsciiGif]] = {
    repository.fetchGif(downloadRequest.url, downloadRequest.size.map(_.targetDefinition).getOrElse(MediumImage))
  }

  private def downloadAndTransform(downloadRequest: DownloadRequest): Future[TransformResult] = {
    gifDownloadService.streamFileBytes(downloadRequest).flatMap {
      byteBuffer =>
        gifReader.ask(TransformRequest(downloadRequest.url, new ByteArrayInputStream(byteBuffer.array()), downloadRequest.size)).mapTo[TransformResult]
    }
  }

}
