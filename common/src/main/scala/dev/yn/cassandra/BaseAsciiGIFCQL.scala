package dev.yn.cassandra

import com.datastax.driver.core.querybuilder.BuiltStatement
import com.datastax.driver.core.{ResultSet, Session}
import dev.yn.size.ImageSize._
import play.api.Logger

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
  * Created by devyn on 8/7/16.
  */
trait BaseAsciiGIFCQL {
  import BaseAsciiGIFCQL._
  private val Log = Logger(classOf[BaseAsciiGIFCQL])

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
    * Execute an arbitrary CQL statement within a session and return the result in a
    * scala future
    *
    * @param session
    * @param statement
    * @return
    */
  protected def executeStatement(session: Session, statement: BuiltStatement): Future[ResultSet] = {
    Future(session.executeAsync(statement).getUninterruptibly)
  }
}

object BaseAsciiGIFCQL {
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

  val asciiKeySpaceCQL = s"CREATE KEYSPACE IF NOT EXISTS $AsciiImageKeyspace " +
    "WITH replication = {'class':'SimpleStrategy', 'replication_factor':1} " +
    "AND DURABLE_WRITES = true;"

  val imageKeyTablesCQL =
    s"CREATE TABLE IF NOT EXISTS $AsciiImageKeyspace.$KeyTable (" +
      s"$UriField text PRIMARY KEY, " +
      s"$IdField uuid);"


  val compositeAsciiImageTableCQL =
    s"CREATE TABLE IF NOT EXISTS $AsciiImageKeyspace.$CompositeTable (" +
      s"$IdField uuid PRIMARY KEY, " +
      s"$TagsField list<text>, " +
      s"$ExtraLargeField blob, " +
      s"$LargeField blob, " +
      s"$MediumField blob, " +
      s"$SmallField blob, " +
      s"$ExtraSmallField blob);"
}
