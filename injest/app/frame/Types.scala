package frame

import play.api.libs.json.{Writes, Reads, Format, Json}

object Types {

  type GenericFrame[T] = Seq[T]

  type GenericGif[T] = Seq[GenericFrame[T]]

  /**
    * Ascii Frame data model
    *
    * Each line of an ascii frame is a String in a Seq
    */
  type AsciiFrame = GenericFrame[String]

  /**
    * Ascii Gif data model
    */
  type AsciiGif = GenericGif[String]
}
