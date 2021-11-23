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
import java.util.concurrent.Executors
import java.util.concurrent.Executor
import com.github.mizosoft.methanol.Methanol
import org.mbari.raziel.ext.methanol.LoggingInterceptor
import org.mbari.raziel.domain.User
import java.net.http.HttpRequest
import java.net.URI

import org.mbari.raziel.etc.circe.CirceCodecs.given
import zio.IO
import org.mbari.raziel.AppConfig
import zio.Task

class VarsUserServer(
    rootUrl: String,
    timeout: Duration = Duration.ofSeconds(10),
    executor: Executor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)
):

  private val httpClientSupport = new HttpClientSupport(timeout, executor)

  object Users:
    def findByName(userName: String): Task[Option[User]] =
      val request = HttpRequest
        .newBuilder()
        .uri(URI.create(s"$rootUrl/users/$userName"))
        .header("Accept", "application/json")
        .GET()
        .build()
      httpClientSupport
        .requestToTask[User](request)
        .map(u => Option(u))

object VarsUserServer:
  def default(using executor: Executor) =
    new VarsUserServer(
      AppConfig.Vars.User.Server.Url.toExternalForm,
      AppConfig.Vars.User.Server.Timeout,
      executor
    )
