package cassandra

import java.util.UUID

import com.datastax.driver.core.querybuilder.{BuiltStatement, Insert}
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => fieldEq}
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.Select.Where
import request.ImageSize._
import scala.collection.JavaConversions._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
  * CQL utilities for saving and retrieving ascii gifs
  */
trait AsciiGifCQL {
  //TODO make configuration
  val FuturePollTimeMillis = 100

  def createTableAsciiKeyspace(implicit session: Session) = {
    session.execute(asciiKeySpaceCQL)
  }

  def createKeyTable(implicit session: Session) = {
    session.execute(imageKeyTablesCQL)
  }

  def createCompositeAsciiImageTable(implicit session: Session) = {
    session.execute(compositeAsciiImageTableCQL)
  }

  def write(image: ImageModel, urlKeyModel: UrlKeyModel)(implicit session: Session): Future[ResultSet] = {
    val keyWriteF = executeStatement(session, InsertUriIdStatement(urlKeyModel))
    val imageWriteF = executeStatement(session, insertImageStatement(image))
    (for {
      keyResult <- keyWriteF;
      imageWriteResult <- imageWriteF
    } yield (keyResult, imageWriteResult)).map {
      case (keyResult, imageWriteResult) =>
        imageWriteResult.all().map { row =>
          println(row)
          row.toString
        }
        println(imageWriteResult)
        imageWriteResult
    }
  }

  //TODO merge the following two methods
  def readKey(url: String)(implicit session: Session): Future[Option[UrlKeyModel]] = {
    executeStatement(session, readKeyStatement(url)).map { resultSet =>
      resultSet.getAvailableWithoutFetching match {
        case 0 => None
        case 1 =>
          val row = resultSet.one()
          Some(UrlKeyModel(row.getString(UriField), row.getUUID(IdField)))
        case _ => throw new RuntimeException("too many results") //TODO better handling
      }
    }
  }

  def readImage(id: UUID, size: ImageSizeDefinition)(implicit session: Session): Future[Option[String]] = {
    executeStatement(session, readImageStatement(id, size)).map {
      resultSet =>
        resultSet.getAvailableWithoutFetching match {
          case 0 => None
          case 1 => Some(resultSet.one().getString(size.fieldName))
          case _ => throw new RuntimeException("too many results")//TODO better handling
        }

    }
  }

  val AsciiImageKeyspace = "ascii_image"
  val KeyTable = "keys"

  val CompositeTable = "composite"
  val UriField = "uri"
  val IdField = "id"
  val TagsField = "tags"
  val ExtraLargeField = "extra_large"
  val LargeField = "large"
  val MediumField = "medium"
  val SmallField = "small"
  val ExtraSmallField = "extra_small"

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

  //TODO: This needs real work
  private def executeStatement(session: Session, statement: BuiltStatement): Future[ResultSet] = {
    Future.successful(session.executeAsync(statement).getUninterruptibly)
  }

  private val asciiKeySpaceCQL = s"CREATE KEYSPACE IF NOT EXISTS ${AsciiImageKeyspace} " +
    "WITH replication = {'class':'SimpleStrategy', 'replication_factor':1} " +
    "AND DURABLE_WRITES = true;"

  private val imageKeyTablesCQL =
    s"CREATE TABLE IF NOT EXISTS ${AsciiImageKeyspace}.${KeyTable} (" +
      s"$UriField text PRIMARY KEY, " +
      s"$IdField uuid);"


  private val compositeAsciiImageTableCQL =
    s"CREATE TABLE IF NOT EXISTS ${AsciiImageKeyspace}.${CompositeTable} (" +
      s"$IdField uuid PRIMARY KEY, " +
      s"$TagsField list<text>, " +
      s"$ExtraLargeField text, " +
      s"$LargeField text, " +
      s"$MediumField text, " +
      s"$SmallField text, " +
      s"$ExtraSmallField text);"

  private def insertImageStatement(image: ImageModel): Insert =
    insertInto(AsciiImageKeyspace, CompositeTable)
      .value(IdField, image.id)
      //.value(ExtraLargeField, image.extraLarge.toString) TODO need to provide cassandra configuration for commitlog_segment_size_in_mb (either 64 or 128 mb)
      .value(LargeField, image.large.toString)
      .value(MediumField, image.medium.toString)
      .value(SmallField, image.small.toString)
      .value(ExtraSmallField, image.extraSmall.toString)

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
