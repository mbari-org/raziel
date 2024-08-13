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

import scala.concurrent.Future
import org.mbari.raziel.domain.HealthStatus
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.ExecutionContext
import org.mbari.raziel.domain.ErrorMsg
import org.mbari.raziel.etc.circe.CirceCodecs.given
import org.mbari.raziel.domain.ServiceStatus

class HealthEndpoints(controller: HealthController, context: String = "config")(using
    ec: ExecutionContext
) extends org.mbari.raziel.api.Endpoints:

    val defaultEndpoint: PublicEndpoint[Unit, ErrorMsg, HealthStatus, Any] =
        baseEndpoint
            .get
            .in(context / "health")
            .out(jsonBody[HealthStatus])
            .name("razielHealth")
            .description("Get the health status of the server")
            .tag("health")
    val defaultImpl: ServerEndpoint[Any, Future]                           =
        defaultEndpoint.serverLogic(Unit => Future(Right(controller.defaultHealthStatus)))

    val expectedEndpoint: PublicEndpoint[Unit, ErrorMsg, Seq[ServiceStatus], Any] =
        baseEndpoint
            .get
            .in(context / "health" / "expected")
            .out(jsonBody[Seq[ServiceStatus]])
            .name("listExpectedServices")
            .description(
                "Get a list of the expected services. This returns services that are expected to be running, but might not be."
            )
            .tag("health")
    val expectedImpl: ServerEndpoint[Any, Future]                                 =
        expectedEndpoint.serverLogic(Unit => Future(Right(controller.expectedServiceStatus)))

    val availableEndpoint: PublicEndpoint[Unit, ErrorMsg, Seq[ServiceStatus], Any] =
        baseEndpoint
            .get
            .in(context / "health" / "available")
            .out(jsonBody[Seq[ServiceStatus]])
            .name("listAvailableServices")
            .description(
                "Get a list of the available services. Services that are down will not be included"
            )
            .tag("health")
    val availableImpl: ServerEndpoint[Any, Future]                                 =
        availableEndpoint.serverLogic(Unit => Future(controller.availableServiceStatus()))

    val statusEndpoint: PublicEndpoint[Unit, ErrorMsg, Seq[ServiceStatus], Any] =
        baseEndpoint
            .get
            .in(context / "health" / "status")
            .out(jsonBody[Seq[ServiceStatus]])
            .name("listAllServices")
            .description(
                "Get a list of the services. Services that are down will not include health status"
            )
            .tag("health")
    val statusImpl: ServerEndpoint[Any, Future]                                 =
        statusEndpoint.serverLogic(Unit => Future(controller.currentServiceStatus()))

    override def all: List[Endpoint[?, ?, ?, ?, ?]] = List(
        defaultEndpoint,
        expectedEndpoint,
        availableEndpoint,
        statusEndpoint
    )
    val allImpl: List[ServerEndpoint[Any, Future]]  = List(
        defaultImpl,
        expectedImpl,
        availableImpl,
        statusImpl
    )
