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

import com.github.mizosoft.methanol.Methanol
import java.net.http.HttpRequest
import java.net.URI
import java.time.Duration
import java.util.concurrent.{Executor, Executors}
import org.mbari.raziel.AppConfig
import org.mbari.raziel.domain.User
import org.mbari.raziel.etc.circe.CirceCodecs.given
import org.mbari.raziel.etc.methanol.HttpClientSupport
import org.mbari.raziel.ext.methanol.LoggingInterceptor
import zio.{IO, Task}
import org.mbari.raziel.domain.HealthStatus

/**
 * Service for accessing the vars-user-server. Needed for validating user credentials.
 * @param rootUrl
 *   The root URL of the vars-user-server e.g. http://localhost:8080/accounts/v1
 * @param timeout
 *   The timeout for the HTTP request
 * @param executor
 *   The executor to use for the HTTP requests
 * @author
 *   Brian Schlining
 */
class VarsUserServer(
    rootUrl: String,
    timeout: Duration,
    executor: Executor = Executors.newSingleThreadExecutor()
) extends HasHealth:

  private val httpClientSupport = new HttpClientSupport(timeout, executor)

  val name = "vars-user-server"

  def health(): Task[HealthStatus] =
    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(s"$rootUrl/health"))
      .header("Accept", "application/json")
      .GET()
      .build()
    httpClientSupport
      .requestObjectsZ[HealthStatus](request)

  object Users:

    /**
     * Find a user in the vars-user-server by thier username
     * @param userName
     *   The username of the user to find
     * @return
     *   A user if found, otherwise None
     */
    def findByName(userName: String): Task[Option[User]] =
      val request = HttpRequest
        .newBuilder()
        .uri(URI.create(s"$rootUrl/users/$userName"))
        .header("Accept", "application/json")
        .GET()
        .build()
      httpClientSupport
        .requestObjectsZ[User](request)
        .map(u => Option(u))

object VarsUserServer:

  /**
   * Builds a VarsUserServer from the application configuration and the provided executor
   * @param executor
   *   The executor to use for the HTTP requests
   */
  def default(using executor: Executor) =
    new VarsUserServer(
      AppConfig.VarsUserServer.Url.toExternalForm,
      AppConfig.VarsUserServer.Timeout,
      executor
    )
