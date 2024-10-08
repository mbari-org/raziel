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
import zio.Task
import org.mbari.raziel.domain.HealthStatus
import zio.ZIO

import java.util.concurrent.Executor

class HealthServices(services: Seq[HealthService]):

    def fetchHealth(): Task[Seq[ServiceStatus]] =
        for healthStati <-
                ZIO.collectAll(
                    services.map(s => s.health()
                        .map(hs => ServiceStatus(s.name, Some(hs))) // Rename the application to the service name
                        .orElse(ZIO.succeed(ServiceStatus(s.name, Some(HealthStatus.empty(s.name))))))
                )
        yield (healthStati :+ ServiceStatus(AppConfig.Name, Some(HealthStatus.default)))
            .sortBy(_.name)

object HealthServices:

    def init(using executor: Executor): Seq[HealthService] = List(
        Annosaurus.default,
        Beholder.default,
        Charybdis.default,
        Oni.default,
        Panoptes.default,
        VampireSquid.default,
        VarsKbServer.default,
        VarsUserServer.default
    ).flatten
