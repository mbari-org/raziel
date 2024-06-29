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

import org.mbari.raziel.domain.{ErrorMsg, NotFound, ServerError, Unauthorized}
import org.mbari.raziel.etc.circe.CirceCodecs.given
import scala.concurrent.Future
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

trait Endpoints:
  val log = System.getLogger(getClass.getName)

  def all: List[Endpoint[?, ?, ?, ?, ?]]
  def allImpl: List[ServerEndpoint[Any, Future]]

  val baseEndpoint: Endpoint[Unit, Unit, ErrorMsg, Unit, Any] = endpoint.errorOut(
    oneOf[ErrorMsg](
      oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
      oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[ServerError])),
      oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized]))
    )
  )
