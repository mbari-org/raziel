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

package org.mbari.raziel.domain

import io.circe.Json
import io.circe.parser.parse
import org.slf4j.LoggerFactory

object HealthStatusHelidon:

    private val log = LoggerFactory.getLogger(getClass)

    def parseString(json: String): Option[HealthStatus] =
        val either = for
            j <- parse(json)
            h <- parseJson(j)
        yield h
        either.toOption

    def parseJson(json: Json): Either[Throwable, HealthStatus] =
        val cursor  = json.hcursor
        val memJson = cursor
            .downField("checks")
            .values
            .map(_.toSeq)
            .getOrElse(Seq.empty)
            .lastOption

        for
            doc         <- memJson.toRight(new Exception("No memory check found"))
            c            = doc.hcursor
            freeMemory  <- c.downField("data").downField("freeBytes").as[Long]
            maxMemory   <- c.downField("data").downField("maxBytes").as[Long]
            totalMemory <- c.downField("data").downField("totalBytes").as[Long]
        yield HealthStatus("unknown", -1, freeMemory, maxMemory, totalMemory, "unknown")
