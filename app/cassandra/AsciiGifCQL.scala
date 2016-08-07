package cassandra

import java.nio.ByteBuffer
import java.util.UUID

import com.datastax.driver.core.querybuilder.{BuiltStatement, Insert}
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => fieldEq}
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.Select.Where
import play.api.Logger
import request.ImageSize._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
  * CQL utilities for saving and retrieving ascii gifs
  */
trait AsciiGifCQL {
  import AsciiGifCQL._

  private final val Log = Logger(classOf[AsciiGifCQL])

  def createTableAsciiKeyspace(implicit session: Session) = {
    session.execute(asciiKeySpaceCQL)
  }

  def createKeyTable(implicit session: Session) = {
    session.execute(imageKeyTablesCQL)
  }

  def createCompositeAsciiImageTable(implicit session: Session) = {
    session.execute(compositeAsciiImageTableCQL)
  }

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

  /**
    * Get the id of an image by the url
    *
    * @param url
    * @param session
    * @return UrlKeyModel for the given url
    */
  def readKey(url: String)(implicit session: Session): Future[Option[UrlKeyModel]] = {
    executeStatement(session, readKeyStatement(url))
      .map(handleSingleValueResult(toUriKeyModel))
  }

  /**
    * read a specific size of an image
    *
    * @param id
    * @param size
    * @param session
    * @return gzip compressed byte buffer for the specified gif and size
    */
  def readImage(id: UUID, size: ImageSizeDefinition)(implicit session: Session): Future[Option[ByteBuffer]] = {
    executeStatement(session, readImageStatement(id, size))
      .map(handleSingleValueResult(toByteBufferForSize(size)))
  }

  private def handleSingleValueResult[T](extractResult: ResultSet => T): ResultSet => Option[T] = {
    resultSet =>
      resultSet.getAvailableWithoutFetching match {
        case 0 => None
        case 1 => Some(extractResult(resultSet))
        case _ =>
          Log.error(s"too many results on query. Expected: 1, Actual: ${resultSet.getAvailableWithoutFetching}")
          Some(extractResult(resultSet))
      }
  }

  private val toUriKeyModel: ResultSet => UrlKeyModel = {
    resultSet =>
      val row = resultSet.one()
      UrlKeyModel(row.getString(UriField), row.getUUID(IdField))
  }

  private val toByteBufferForSize: ImageSizeDefinition => ResultSet => ByteBuffer =
    size => resultSet => resultSet.one().getBytes(size.fieldName)


  /**
    * Execute an arbitrary CQL statement within a session and return the result in a
    * scala future
    *
    * @param session
    * @param statement
    * @return
    */
  private def executeStatement(session: Session, statement: BuiltStatement): Future[ResultSet] = {
    Future(session.executeAsync(statement).getUninterruptibly)
  }
}

object AsciiGifCQL {
  implicit class ImageSizeDefnitionUtil(sizeDefinition: ImageSizeDefinition) {
    def fieldName: String = {
      sizeDefinition match {
        case ExtraSmallImage => ExtraSmallField
        case SmallImage => SmallField
        case MediumImage => MediumField
        case LargeImage => LargeField
        case ExtraLargeImage => ExtraLargeField
      }
    }
  }

  private val AsciiImageKeyspace = "ascii_image"
  private val KeyTable = "keys"

  private val CompositeTable = "composite"
  private val UriField = "uri"
  private val IdField = "id"
  private val TagsField = "tags"
  private val ExtraLargeField = "extra_large"
  private val LargeField = "large"
  private val MediumField = "medium"
  private val SmallField = "small"
  private val ExtraSmallField = "extra_small"

  private val asciiKeySpaceCQL = s"CREATE KEYSPACE IF NOT EXISTS $AsciiImageKeyspace " +
    "WITH replication = {'class':'SimpleStrategy', 'replication_factor':1} " +
    "AND DURABLE_WRITES = true;"

  private val imageKeyTablesCQL =
    s"CREATE TABLE IF NOT EXISTS $AsciiImageKeyspace.$KeyTable (" +
      s"$UriField text PRIMARY KEY, " +
      s"$IdField uuid);"


  private val compositeAsciiImageTableCQL =
    s"CREATE TABLE IF NOT EXISTS $AsciiImageKeyspace.$CompositeTable (" +
      s"$IdField uuid PRIMARY KEY, " +
      s"$TagsField list<text>, " +
      s"$ExtraLargeField blob, " +
      s"$LargeField blob, " +
      s"$MediumField blob, " +
      s"$SmallField blob, " +
      s"$ExtraSmallField blob);"

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

  private def readKeyStatement(url: String): Where = {
    select(Seq(UriField, IdField): _*).from(AsciiImageKeyspace, KeyTable)
      .where(fieldEq(UriField, url))
  }

  private def readImageStatement(id: UUID, size: ImageSizeDefinition): Where = {
    select(Seq(IdField, size.fieldName): _*).from(AsciiImageKeyspace, CompositeTable)
      .where(fieldEq(IdField, id))
  }
}