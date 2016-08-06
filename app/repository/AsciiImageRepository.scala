package repository

import cassandra.{UrlKeyModel, CassandraConnector, ImageModel, AsciiGifCQL}
import play.api.Logger
import play.api.libs.json.Json
import request.ImageSize.ImageSizeDefinition

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class AsciiImageRepository extends CassandraConnector("cassandra") with AsciiGifCQL {
  val Log = Logger(classOf[AsciiGifCQL])

  createTableAsciiKeyspace
  createKeyTable
  createCompositeAsciiImageTable

  def insertGif(imageModel: ImageModel, urlKeyModel: UrlKeyModel) = {
    Log.info(s"inserting model for: ${urlKeyModel}")
    write(imageModel, urlKeyModel)
  }

  def fetchGif(url: String, sizeDefinition: ImageSizeDefinition): Future[Option[Seq[Seq[String]]]] = {
    Log.info(s"reading model for: $url of size $sizeDefinition")
    for{
      keyOption <- readKey(url);
      resultOption <-  keyOption.map { key =>
        readImage(key.id, sizeDefinition)
      }.getOrElse(Future.successful(None))
    } yield {
      resultOption.flatMap(resultString => Json.parse(resultString).validate[Seq[Seq[String]]].asEither match {
        case Right(success) => Some(success)
        case Left(error) =>
          Log.error(s"could not parse db result: $error")
          Log.debug(s"db result value: $resultString")
          None
      })
    }
  }
}
