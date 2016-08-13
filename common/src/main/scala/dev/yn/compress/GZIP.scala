package dev.yn.compress

import java.io._
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

object GZIP {
  def compressedOutputStream(s: String): NonCopyingByteArrayOutputStream = {
    val outputStream = new NonCopyingByteArrayOutputStream(s.length)
    val writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(outputStream)));
    writer.append(s)
    writer.close()
    outputStream
  }

  def compress(s: String): Array[Byte] = {
    val outputStream = compressedOutputStream(s)
    outputStream.getBuf.take(outputStream.getCount)
  }

  def decompressedOutputStream(gzip: Array[Byte]) = {
    val inputStream = new GZIPInputStream(new ByteArrayInputStream(gzip))
    val out = new NonCopyingByteArrayOutputStream(gzip.length * 10)
    val buffer: Array[Byte] = new Array(1024)
    var in = inputStream.read(buffer)
    while(in > 0) {
      out.write(buffer, 0, in)
      in = inputStream.read(buffer)
    }
    out
  }

  def decompress(bytes: Array[Byte]): String = {
    val outputStream = decompressedOutputStream(bytes)
    new String(outputStream.getBuf.take(outputStream.getCount))
  }
}

/**
  * Byte array outpust stream that allows direct access to the buffer
  */
class NonCopyingByteArrayOutputStream(size: Int) extends ByteArrayOutputStream(size) {
  def getCount = count
  def getBuf = buf
}
