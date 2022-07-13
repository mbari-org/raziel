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

import io.circe.generic.auto._
import org.mbari.raziel.domain.{BearerAuth, ErrorMsg}
import org.mbari.raziel.domain.EndpointConfig
import org.mbari.raziel.etc.circe.CirceCodecs.given
import scala.concurrent.{ExecutionContext, Future}
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import java.net.URL
import scala.util.Try

/**
 * Returns infomation about the proxied endpoints
 *
 * ## /endpoints (with missing or invalid Authorization header)
 *
 * ### Request
 *
 * ```text
 * GET /endpoints
 * ```
 * ### Response 200
 *
 * ```text
 * HTTP/1.1 200 OK
 * Connection: close
 * Date: Wed, 24 Nov 2021 06:40:53 GMT
 * Content-Type: application/json;charset=utf-8
 * Content-Length: 653
 * Server: Jetty(9.4.44.v20210927)
 *
 * [
 * {
 * "name": "annosaurus",
 * "url": "http://m3.shore.mbari.org/anno/v1",
 * "timeout": "PT10S",
 * "proxyPath": "/anno"
 * },
 * {
 * "name": "charybdis",
 * "url": "http://m3.shore.mbari.org/references",
 * "timeout": "PT30S",
 * "proxyPath": "/references"
 * },
 * {
 * "name": "panoptes",
 * "url": "http://singularity.shore.mbari.org:8080/panoptes",
 * "timeout": "PT10S",
 * "proxyPath": "/panoptes"
 * },
 * {
 * "name": "vampire-squid",
 * "url": "http://m3.shore.mbari.org/vam/v1",
 * "timeout": "PT10S",
 * "proxyPath": "/vam"
 * },
 * {
 * "name": "vars-kb-server",
 * "url": "http://m3.shore.mbari.org/kb/v1",
 * "timeout": "PT20S",
 * "proxyPath": "/kb"
 * },
 * {
 * "name": "vars-user-server",
 * "url": "http://m3.shore.mbari.org/accounts/v1",
 * "timeout": "PT10S",
 * "proxyPath": "/accounts"
 * }
 * ]
 * ```
 *
 * ## /endpoints (with valid Authorization header)
 *
 * ### Request
 *
 * ```text
 * GET /endpoints
 * Authorization: Bearer <JWT>
 * ```
 *
 * ### Response 200
 *
 * ```text
 * HTTP/1.1 200 OK
 * Connection: close
 * Date: Wed, 24 Nov 2021 06:42:04 GMT
 * Content-Type: application/json;charset=utf-8
 * Content-Length: 653
 * Server: Jetty(9.4.44.v20210927)
 *
 * [
 * {
 * "name": "annosaurus",
 * "url": "http://m3.shore.mbari.org/anno/v1",
 * "timeout": "PT10S",
 * "proxyPath": "/anno",
 * "secret": "secret"
 * },
 * {
 * "name": "charybdis",
 * "url": "http://m3.shore.mbari.org/references",
 * "timeout": "PT30S",
 * "proxyPath": "/references"
 * },
 * {
 * "name": "panoptes",
 * "url": "http://singularity.shore.mbari.org:8080/panoptes",
 * "timeout": "PT10S",
 * "proxyPath": "/panoptes",
 * "secret": "secret"
 * },
 * {
 * "name": "vampire-squid",
 * "url": "http://m3.shore.mbari.org/vam/v1",
 * "timeout": "PT10S",
 * "proxyPath": "/vam",
 * "secret": "secret"
 * },
 * {
 * "name": "vars-kb-server",
 * "url": "http://m3.shore.mbari.org/kb/v1",
 * "timeout": "PT20S",
 * "proxyPath": "/kb"
 * },
 * {
 * "name": "vars-user-server",
 * "url": "http://m3.shore.mbari.org/accounts/v1",
 * "timeout": "PT10S",
 * "proxyPath": "/accounts"
 * }
 * ]
 *
 * ```
 *
 * @author
 *   Brian Schlining
 * @since 2021-12-23T11:00:00
 */
class EndpointsEndpoints(using ec: ExecutionContext) extends org.mbari.raziel.api.Endpoints:

  given Schema[URL] = Schema.string

  val endpoints: PublicEndpoint[Option[String], ErrorMsg, List[EndpointConfig], Any] =
    baseEndpoint
      .get
      .in("endpoints")
      .in(header[Option[String]]("Authorization"))
      .out(jsonBody[List[EndpointConfig]])
      .description(
        "List available endpoints. Authorization header is optional. If defined it returns connection information for the endpoint."
      )
  val endpointsImpl: ServerEndpoint[Any, Future]                                     =
    endpoints.serverLogic(authOpt => Future(Right(EndpointsController.getEndpoints(authOpt))))

  override val all: List[PublicEndpoint[?, ?, ?, ?]]      = List(endpoints)
  override val allImpl: List[ServerEndpoint[Any, Future]] = List(endpointsImpl)