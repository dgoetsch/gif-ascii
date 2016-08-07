package cassandra

import com.datastax.driver.core.querybuilder.Insert
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core._
import dev.yn.cassandra.{BaseAsciiGifCQL, UrlKeyModel, CompressedImageModel}
import play.api.Logger
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
  * CQL utilities for saving and retrieving ascii gifs
  */
trait AsciiGifWriteCQL extends BaseAsciiGifCQL {
  import AsciiGifWriteCQL._

  private final val Log = Logger(classOf[AsciiGifWriteCQL])

  /**
    * Write all sizes of a compressed ascii gif to the db
    *
    * @param image
    * @param urlKeyModel
    * @param session
    * @return
    */
  def write(image: CompressedImageModel, urlKeyModel: UrlKeyModel)(implicit session: Session): Future[Unit] = {
    val keyWriteF = executeStatement(session, InsertUriIdStatement(urlKeyModel))
    val imageWriteF = executeStatement(session, insertImageStatement(image))
    (for {
      keyResult <- keyWriteF;
      imageWriteResult <- imageWriteF
    } yield (keyResult, imageWriteResult)).map {
      case (keyResult, imageWriteResult) =>
        Unit
    }
  }
}

object AsciiGifWriteCQL {
  import BaseAsciiGifCQL._

  private def insertImageStatement(image: CompressedImageModel): Insert =
    insertInto(AsciiImageKeyspace, CompositeTable)
      .value(IdField, image.id)
      .value(ExtraLargeField, image.extraLarge)
      .value(LargeField, image.large)
      .value(MediumField, image.medium)
      .value(SmallField, image.small)
      .value(ExtraSmallField, image.extraSmall)

  private def InsertUriIdStatement(urlKeyModel: UrlKeyModel): Insert =
    insertInto(AsciiImageKeyspace, KeyTable)
      .value(UriField, urlKeyModel.uri)
      .value(IdField, urlKeyModel.id)
}