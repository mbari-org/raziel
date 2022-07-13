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

import org.mbari.raziel.etc.auth0.JwtHelper
import org.mbari.raziel.domain.EndpointConfig
import org.mbari.raziel.domain.ErrorMsg
import org.mbari.raziel.domain.{BearerAuth, ErrorMsg, Unauthorized}

object EndpointsController:

  private val jwtHelper = JwtHelper.default

  private val securedEndpoints: List[EndpointConfig]   = EndpointConfig.defaults
  private val unsecuredEndpoints: List[EndpointConfig] =
    EndpointConfig.defaults.map(_.copy(secret = None))

  private def authenticate(authHeader: Option[String]): Boolean =
    val auth = authHeader
      .flatMap(a => BearerAuth.parse(a))
      .toRight(new IllegalArgumentException("Authorization header required"))

    val either = for
      a          <- auth
      decodedJwt <- jwtHelper.verifyJwt(a.accessToken)
    yield decodedJwt

    either.isRight

  def getEndpoints(authHeader: Option[String]): List[EndpointConfig] =
    if (authenticate(authHeader))
      securedEndpoints
    else
      unsecuredEndpoints
