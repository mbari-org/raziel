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

package org.mbari.raziel.services

import org.mbari.raziel.AppConfig
import org.mbari.raziel.domain.ServiceStatus
import org.mbari.raziel.domain.HealthStatus

import java.util.concurrent.Executor
import org.mbari.raziel.etc.sdk.Eithers
import scala.util.Try
import java.security.Provider.Service
import scala.util.{Failure, Success}

class HealthServices(services: Seq[HealthService]):

    def fetchHealth(): Seq[ServiceStatus] =

        val healthStati =
            for service <- services
            yield service.health() match
                case Right(healthStatus) => ServiceStatus(service.name, Some(healthStatus))
                case Left(_)             =>
                    ServiceStatus(service.name, Some(HealthStatus.empty(service.name)))

        (healthStati :+ ServiceStatus(AppConfig.Name, Some(HealthStatus.default)))
            .sortBy(_.name)

object HealthServices:

    def init(using executor: Executor): Seq[HealthService] = List(
        Annosaurus.default,
        Beholder.default,
        Charybdis.default,
        Oni.default,
        Panoptes.default,
        Skimmer.default,
        VampireSquid.default,
        VarsKbServer.default,
        VarsUserServer.default
    ).flatten
