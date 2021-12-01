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

package org.mbari.raziel.api

import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.mbari.raziel.domain.HealthStatus
import org.mbari.raziel.etc.circe.CirceCodecs.{given, _}
import org.mbari.raziel.services.HasHealth
import org.scalatra.ScalatraServlet
import zio.ZIO
import zio.Task
import scala.util.{Failure, Success, Try}
import org.scalatra.InternalServerError
import org.mbari.raziel.domain.ErrorMsg

/**
 * Health status API
 *
 * ## /health
 *
 * ### Request
 *
 * ```text
 * GET /health
 * ```
 *
 * ### Response 200
 *
 * ```text
 * HTTP/1.1 200 OK
 * Connection: close
 * Date: Wed, 24 Nov 2021 06:48:17 GMT
 * Content-Type: text/plain;charset=utf-8
 * Content-Length: 145
 * Server: Jetty(9.4.44.v20210927)
 *
 * {
 * "jdkVersion": "17.0.1+12-39",
 * "availableProcessors": 10,
 * "freeMemory": 21093224,
 * "maxMemory": 1073741824,
 * "totalMemory": 46137344,
 * "application": "raziel"
 * }
 *
 * ```
 *
 * @author
 *   Brian Schlining
 * @since 2021-11-23T11:00:00
 */
class HealthApi(services: Seq[HasHealth]) extends ScalatraServlet:

  private val runtime   = zio.Runtime.default

  after() {
    contentType = "application/json"
  }

  get("/") {
    HealthStatus.default.stringify
  }

  get("/services") {
    val app = for 
      healthStati <- Task.collectAll(services.map(_.health()))
    yield
      healthStati

    Try(runtime.unsafeRun(app)) match
      case Success(healthStati) =>
        healthStati.stringify
      case Failure(e) =>
        halt(InternalServerError(ErrorMsg(e.getMessage, 401).stringify))


  }
