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

import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.mbari.raziel.etc.methanol.HttpClientSupport
import zio.Task
import java.net.http.HttpRequest
import org.mbari.raziel.domain.HealthStatus
import java.net.URI
import org.mbari.raziel.etc.circe.CirceCodecs.given
import org.mbari.raziel.AppConfig

object Beholder:

    def default(using executor: Executor): Option[HealthService] =
        AppConfig
            .Beholder
            .map(config =>
                val uri = URI.create(s"${config.internalUrl.toExternalForm}/health")
                new DefaultHealthService(
                    config.name,
                    uri,
                    config.timeout,
                    executor
                )
            )
