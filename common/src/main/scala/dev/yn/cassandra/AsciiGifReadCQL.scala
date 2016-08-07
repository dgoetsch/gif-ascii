package dev.yn.cassandra

import java.nio.ByteBuffer
import java.util.UUID

import com.datastax.driver.core.{ResultSet, Session}
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.Select.Where
import dev.yn.size.ImageSize.ImageSizeDefinition
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => fieldEq}
import play.api.Logger

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

trait AsciiGifReadCQL extends BaseAsciiGifCQL {
  import AsciiGifReadCQL._

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
}

object AsciiGifReadCQL {
  import BaseAsciiGifCQL._
  private val Log = Logger(classOf[AsciiGifReadCQL])

  private def readKeyStatement(url: String): Where = {
    select(Seq(UriField, IdField): _*).from(AsciiImageKeyspace, KeyTable)
      .where(fieldEq(UriField, url))
  }

  private def readImageStatement(id: UUID, size: ImageSizeDefinition): Where = {
    select(Seq(IdField, size.fieldName): _*).from(AsciiImageKeyspace, CompositeTable)
      .where(fieldEq(IdField, id))
  }

  private val toUriKeyModel: ResultSet => UrlKeyModel = {
    resultSet =>
      val row = resultSet.one()
      UrlKeyModel(row.getString(UriField), row.getUUID(IdField))
  }

  private val toByteBufferForSize: ImageSizeDefinition => ResultSet => ByteBuffer =
    size => resultSet => resultSet.one().getBytes(size.fieldName)

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
}
