package compress

import java.io._
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

object GZIP {
  def outputStream(s: String): ByteArrayOutputStream = {
    val outputStream = new ByteArrayOutputStream(s.length)
    val writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(outputStream)));
    writer.append(s)
    writer.close()
    outputStream
  }

  def write(s: String): Array[Byte] = {
    outputStream(s).toByteArray
  }

  def byteStream(gzip: Array[Byte]) = {
    val inputStream = new GZIPInputStream(new ByteArrayInputStream(gzip))
    val out = new ByteArrayOutputStream()
    val buffer: Array[Byte] = new Array(1024)
    var in = inputStream.read(buffer)
    while(in > 0) {
      out.write(buffer, 0, in)
      in = inputStream.read(buffer)
    }
    out
  }

  def read(bytes: Array[Byte]): String = {
    new String(byteStream(bytes).toByteArray)
  }
}
