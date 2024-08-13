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

import java.net.http.HttpRequest
import java.net.URI
import java.time.Duration
import java.util.concurrent.{Executor, Executors}
import org.mbari.raziel.AppConfig
import org.mbari.raziel.domain.HealthStatus
import org.mbari.raziel.etc.circe.CirceCodecs.given
import org.mbari.raziel.etc.methanol.HttpClientSupport
import zio.Task
import org.mbari.raziel.domain.HealthStatusHelidon
import zio.ZIO

class Charybdis(
    rootUrl: String,
    timeout: Duration,
    executor: Executor = Executors.newSingleThreadExecutor()
) extends HealthService:

    private val httpClientSupport = new HttpClientSupport(timeout, executor)

    val name = "charybdis"

    val healthUri: URI = URI.create(s"$rootUrl/health")

    def health(): Task[HealthStatus] =

        val request0 = HttpRequest
            .newBuilder()
            .uri(healthUri)
            .header("Accept", "application/json")
            .GET()
            .build()

        val request1 = HttpRequest
            .newBuilder()
            .uri(URI.create(s"$rootUrl/observe/health"))
            .header("Accept", "application/json")
            .GET()
            .build()

        for
            // Try the new endpoint first, fall back to the old one
            body         <- httpClientSupport.requestStringZ(request1).orElse(httpClientSupport.requestStringZ(request0))
            healthStatus <- ZIO.fromEither(
                                HealthStatusHelidon
                                    .parseString(body)
                                    .map(Right(_))
                                    .getOrElse(Left(new Exception(s"Could not parse $body")))
                            )
        yield healthStatus.copy(application = name, description = "Publication Dataset Server")

object Charybdis:

    def default(using executor: Executor): Option[HealthService] =
        AppConfig
            .Charybdis
            .map(config =>
                new Charybdis(
                    config.internalUrl.toExternalForm,
                    config.timeout,
                    executor
                )
            )
