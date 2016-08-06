package service

import java.nio.ByteBuffer
import javax.inject.{Inject, Singleton}

import controllers.DownloadRequest
import play.api.Logger
import play.api.libs.ws.WSClient

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class NetworkDataService @Inject()(wSClient: WSClient) {
  implicit val system = ActorSystem("download-manager")
  implicit val materializer = ActorMaterializer()
  val Log = Logger(classOf[NetworkDataService])

  def streamFileBytes(downloadRequest: DownloadRequest): Future[ByteBuffer] = {
    Log.info("starting download request")
    val time = System.currentTimeMillis()
    wSClient.url(downloadRequest.url).withMethod("GET").stream().flatMap {
      streamedResponse =>
        streamedResponse.headers.headers.get("Content-Length").flatMap(_.headOption)
          match {
            case Some(contentLength) =>
              streamedResponse.body.runWith(
                Sink.fold[ByteBuffer, ByteString]
                  (ByteBuffer.allocate(contentLength.toInt)) {
                  (buffer, bytes) => buffer.put(bytes.asByteBuffer)})
            case _ =>
              streamedResponse.body.runWith(
                Sink.fold[Seq[ByteBuffer], ByteString]
                  (Seq())
                  { (buffers, bytes) => buffers :+ bytes.asByteBuffer }
              ).map { byteBuffers =>
                  val buffer = ByteBuffer.allocate(byteBuffers.map(_.limit).sum)
                  byteBuffers.foreach { subBuffer => buffer.put(subBuffer) }
                  buffer
              }
        }
    }.map {
      result =>
        Log.info(s"took ${System.currentTimeMillis() - time} millis to download ${downloadRequest}")
        result
    }
  }
}
