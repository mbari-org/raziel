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

import org.scalatra.ScalatraServlet
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import org.mbari.raziel.domain.{Authorization, EndpointConfig}
import org.mbari.raziel.etc.auth0.JwtHelper
import org.mbari.raziel.AppConfig
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.mbari.raziel.etc.circe.CirceCodecs.{given, _}

class EndpointsApi extends ScalatraServlet:

  private val jwtHelper = JwtHelper.default
  private val log       = LoggerFactory.getLogger(getClass)

  private val securedEndpoints: List[EndpointConfig]   = EndpointConfig.defaults
  private val unsecuredEndpoints: List[EndpointConfig] =
    EndpointConfig.defaults.map(_.copy(secret = None))

  after() {
    contentType = "application/json"
  }

  private def authenticate(request: HttpServletRequest): Boolean =
    val auth = Option(request.getHeader("Authorization"))
      .flatMap(a => Authorization.parse(a))
      .toRight(new IllegalArgumentException("Authorization header required"))

    val either = for
      a          <- auth
      decodedJwt <- jwtHelper.verifyJwt(a.accessToken)
    yield decodedJwt

    either.isRight

  get("/") {
    val ok = authenticate(request)
    val ep = if (ok) securedEndpoints else unsecuredEndpoints
    ep.asJson.print

  }
