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

import java.net.URL
import java.time.Duration
import org.mbari.raziel.AppConfig

/**
 * Parameters for a microservice endpoint
 * @param name
 *   Name of the microservice app (e.g. "annosaurus")
 * @param url
 *   URL of the endpoint (e.g. "http://localhost:8080/anno/v1")
 * @param timeout
 *   Timeout for the endpoint
 * @param secret
 *   THe secret for authentication for the endpoint
 * @param proxyPath
 *   The path to mount the remote url as in raziel (e.g. "/anno")
 *
 * @author
 *   Brian Schlining
 */
case class EndpointConfig(
    name: String,
    url: URL,
    timeout: Duration,
    secret: Option[String],
    proxyPath: String
)

object EndpointConfig:

  /**
   * @return
   *   A list of M3 microservice [[EndpointConfig]]s as defined in application.conf
   */
  def defaults: List[EndpointConfig] =
    AppConfig.Annosaurus.Endpoint ::
      AppConfig.Charybdis.Endpoint ::
      AppConfig.Panoptes.Endpoint ::
      AppConfig.VampireSquid.Endpoint ::
      AppConfig.VarsKbServer.Endpoint ::
      AppConfig.VarsUserServer.Endpoint ::
      Nil
