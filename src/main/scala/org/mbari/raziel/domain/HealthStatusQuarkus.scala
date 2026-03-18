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

object HealthStatusQuarkus:

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
            doc                 <- memJson.toRight(new Exception("No memory check found"))
            c                    = doc.hcursor
            jdkVersion          <- c.downField("data").downField("jdkVersion").as[String].orElse(Right("unknown"))
            availableProcessors <- c.downField("data").downField("availableProcessors").as[Int].orElse(Right(-1))
            freeMemory          <- c.downField("data").downField("freeMemory").as[Long].orElse(Right(-1L))
            maxMemory           <- c.downField("data").downField("maxMemory").as[Long].orElse(Right(-1L))
            totalMemory         <- c.downField("data").downField("totalMemory").as[Long].orElse(Right(-1L))
            application         <- c.downField("data").downField("application").as[String].orElse(Right("unknown"))
            version             <- c.downField("data").downField("version").as[String].orElse(Right("0.0.0"))
            description         <- c.downField("data").downField("description").as[String].orElse(Right(""))
        yield HealthStatus(
            jdkVersion,
            availableProcessors,
            freeMemory,
            maxMemory,
            totalMemory,
            application,
            version,
            description
        )
