/*
 * Copyright 2021 MBARI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.raziel.etc.circe

import io.circe.*
import io.circe.generic.semiauto.*
import java.net.{URI, URL}
import org.mbari.raziel.domain.{BearerAuth, EndpointConfig, ErrorMsg, HealthStatus, ServiceStatus, User}
import org.mbari.raziel.util.HexUtil
import scala.util.Try
import java.time.Duration
import org.mbari.raziel.domain.SerializedEndpointConfig
import org.mbari.raziel.domain.{ErrorMsg, NotFound, ServerError, Unauthorized}
import org.mbari.raziel.domain.StatusMsg

object CirceCodecs:

    given Encoder[Array[Byte]] = new Encoder[Array[Byte]]:
        final def apply(xs: Array[Byte]): Json =
            Json.fromString(HexUtil.toHex(xs))
    given Decoder[Array[Byte]] = Decoder
        .decodeString
        .emapTry(str => Try(HexUtil.fromHex(str)))

    given Decoder[URL] = Decoder
        .decodeString
        .emapTry(str => Try(URI.create(str).toURL))
    given Encoder[URL] = Encoder
        .encodeString
        .contramap(_.toString)

    given Decoder[URI] = Decoder
        .decodeString
        .emapTry(s => Try(URI.create(s)))
    given Encoder[URI] = Encoder
        .encodeString
        .contramap[URI](_.toString)

    given Decoder[Duration] = Decoder
        .decodeLong
        .emapTry(lng => Try(Duration.ofMillis(lng)))
    given Encoder[Duration] = Encoder
        .encodeLong
        .contramap(_.toMillis)

    given Decoder[User] = deriveDecoder
    given Encoder[User] = deriveEncoder

    given Decoder[BearerAuth] = deriveDecoder
    given Encoder[BearerAuth] = deriveEncoder

    given Decoder[ErrorMsg] = deriveDecoder
    given Encoder[ErrorMsg] = deriveEncoder

    given Decoder[SerializedEndpointConfig] = deriveDecoder
    given Encoder[SerializedEndpointConfig] = deriveEncoder

    given Decoder[HealthStatus] = deriveDecoder
    given Encoder[HealthStatus] = deriveEncoder

    given Decoder[ServiceStatus] = deriveDecoder
    given Encoder[ServiceStatus] = deriveEncoder

    given Decoder[StatusMsg] = deriveDecoder
    given Encoder[StatusMsg] = deriveEncoder

    given Decoder[NotFound] = deriveDecoder
    given Encoder[NotFound] = deriveEncoder

    given Decoder[ServerError] = deriveDecoder
    given Encoder[ServerError] = deriveEncoder

    given Decoder[Unauthorized] = deriveDecoder
    given Encoder[Unauthorized] = deriveEncoder

    private val printer = Printer.noSpaces.copy(dropNullValues = true)

    /**
     * Convert a circe Json object to a JSON string
     * @param value
     *   Any value with an implicit circe coder in scope
     */
    extension (json: Json) def stringify: String = printer.print(json)

    /**
     * Convert an object to a JSON string
     * @param value
     *   Any value with an implicit circe coder in scope
     */
    extension [T: Encoder](value: T) def stringify: String = Encoder[T].apply(value).stringify
