package repository

import cassandra._
import config.ConfigurationHelper
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import request.ImageSize.ImageSizeDefinition

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class AsciiImageRepository(override val configuration: Configuration) extends CassandraConnector with AsciiGifCQL {
  import frame.Types._
  val Log = Logger(classOf[AsciiGifCQL])

  createTableAsciiKeyspace
  createKeyTable
  createCompositeAsciiImageTable

  def insertGif(imageModel: ImageModel, urlKeyModel: UrlKeyModel) = {
    Log.info(s"inserting model for: ${urlKeyModel}")
    write(imageModel.compress, urlKeyModel)
  }

  def fetchGif(url: String, sizeDefinition: ImageSizeDefinition): Future[Option[AsciiGif]] = {
    Log.info(s"reading model for: $url of size $sizeDefinition")
    for{
      keyOption <- readKey(url);
      resultOption <-  keyOption.map { key =>
        readImage(key.id, sizeDefinition)
      }.getOrElse(Future.successful(None))
    } yield {
      resultOption
        .map(ImageModel.decompress)
        .flatMap(extractJson)
    }
  }

  private def extractJson(decompressedDbValue: String): Option[AsciiGif] = {
    Json.parse(decompressedDbValue).validate[Seq[Seq[String]]].asEither match {
      case Right(success) => Some(success.asInstanceOf[AsciiGif]) //TODO clean up types, json difficulties
      case Left(error) =>
        Log.error(s"could not parse db result: $error")
        Log.debug(s"db result value: $decompressedDbValue")
        None
    }
  }
}
