/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.etc.circe

import io.circe.*
import io.circe.generic.semiauto.*
import scala.util.Try
import java.net.URL
import org.mbari.raziel.util.HexUtil
import java.net.URI
import org.mbari.raziel.domain.{Authorization, EndpointConfig, ErrorMsg, User}

object CirceCodecs:

  given byteArrayEncoder: Encoder[Array[Byte]] = new Encoder[Array[Byte]]:
    final def apply(xs: Array[Byte]): Json =
      Json.fromString(HexUtil.toHex(xs))
  given byteArrayDecoder: Decoder[Array[Byte]] = Decoder
    .decodeString
    .emapTry(str => Try(HexUtil.fromHex(str)))

  given urlDecoder: Decoder[URL] = Decoder
    .decodeString
    .emapTry(str => Try(new URL(str)))
  given urlEncoder: Encoder[URL] = Encoder
    .encodeString
    .contramap(_.toString)

  given Decoder[URI] = Decoder
    .decodeString
    .emapTry(s => Try(URI.create(s)))
  given Encoder[URI] = Encoder
    .encodeString
    .contramap[URI](_.toString)

  given Decoder[User] = deriveDecoder
  given Encoder[User] = deriveEncoder

  given Decoder[Authorization] = deriveDecoder
  given Encoder[Authorization] = deriveEncoder

  given Decoder[ErrorMsg] = deriveDecoder
  given Encoder[ErrorMsg] = deriveEncoder

  given Decoder[EndpointConfig] = deriveDecoder
  given Encoder[EndpointConfig] = deriveEncoder

  private val printer = Printer.noSpaces.copy(dropNullValues = true)

  extension (json: Json)
    def print: String = printer.print(json)