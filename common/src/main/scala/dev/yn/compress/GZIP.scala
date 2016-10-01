package dev.yn.compress

import java.io._
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

object GZIP {
  def compressStringToStream(s: String): ByteArrayOutputStream = {
    val outputStream = new ByteArrayOutputStream(s.length)
    val writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(outputStream)));
    writer.append(s)
    writer.close()
    outputStream
  }

  def compressString(s: String): Array[Byte] = {
    compressStringToStream(s).toByteArray
  }

  def decompressStringToStream(gzip: Array[Byte]) = {
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

  def decompress(bytes: Array[Byte]): String = {
    new String(decompressStringToStream(bytes).toByteArray)
  }
}
