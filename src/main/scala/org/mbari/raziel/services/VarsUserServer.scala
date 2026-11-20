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
import io.circe.parser.parse
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.util.Try
import org.mbari.raziel.domain.BasicAuth
import java.time.Duration
import java.util.concurrent.{Executor, Executors}
import org.mbari.raziel.AppConfig
import org.mbari.raziel.domain.User
import org.mbari.raziel.etc.circe.CirceCodecs.given
import org.mbari.raziel.etc.methanol.HttpClientSupport
import org.mbari.raziel.ext.methanol.LoggingInterceptor
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
) extends HealthService:

    private val httpClientSupport = new HttpClientSupport(timeout, executor)

    val name = "vars-user-server"

    val healthUri: URI = URI.create(s"$rootUrl/health")

    def health(): Either[Throwable, HealthStatus] =
        val request = HttpRequest
            .newBuilder()
            .uri(healthUri)
            .header("Accept", "application/json")
            .GET()
            .build()
        httpClientSupport
            .requestObjects[HealthStatus](request)

    object Users:

        /**
         * Find a user in the vars-user-server by thier username
         * @param userName
         *   The username of the user to find
         * @return
         *   A user if found, otherwise None
         */
        def findByName(userName: String): Either[Throwable, Option[User]] =
            val request = HttpRequest
                .newBuilder()
                .uri(URI.create(s"$rootUrl/users/$userName"))
                .header("Accept", "application/json")
                .GET()
                .build()
            httpClientSupport
                .requestObjects[User](request)
                .map(u => Option(u))

    object Authentication:

        /**
         * Authenticate a user by calling Oni's `/auth/login` endpoint (rootUrl is expected to end with `/v1`) using
         * Basic Auth. If Oni responds with a JWT, the user is authenticated. (Oni no longer returns passwords, so we
         * can't check them ourselves.)
         *
         * @param auth
         *   The user's credentials
         * @return
         *   Right(true) if Oni issued a JWT, Right(false) if the credentials were rejected (401/403), or Left if the
         *   request failed or the response was unexpected (any other status, including 404, or a 200 without an
         *   access_token), so misconfiguration is visible rather than reported as bad credentials.
         */
        def authenticate(auth: BasicAuth): Either[Throwable, Boolean] =
            val credentials = Base64
                .getEncoder
                .encodeToString(s"${auth.username}:${auth.password}".getBytes(StandardCharsets.UTF_8))
            val request     = HttpRequest
                .newBuilder()
                .uri(URI.create(s"$rootUrl/auth/login"))
                .header("Accept", "application/json")
                .header("Authorization", s"Basic $credentials")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build()

            Try(httpClientSupport.client.send(request, BodyHandlers.ofString())).toEither.flatMap(response =>
                response.statusCode() match
                    case 200       =>
                        extractToken(response.body()).filter(_.nonEmpty) match
                            case Some(_) => Right(true)
                            case None    =>
                                Left(
                                    new RuntimeException(
                                        s"Response from ${request.uri} was 200 but contained no access_token"
                                    )
                                )
                    case 401 | 403 => Right(false)
                    case code      =>
                        Left(new RuntimeException(s"Unexpected response ($code) from ${request.uri}"))
            )

        private def extractToken(body: String): Option[String] =
            parse(body)
                .toOption
                .flatMap(_.hcursor.get[String]("access_token").toOption)

object VarsUserServer:

    /**
     * Builds a VarsUserServer from the application configuration and the provided executor
     * @param executor
     *   The executor to use for the HTTP requests
     */
    def default(using executor: Executor): Option[VarsUserServer] =
        AppConfig
            .VarsUserServer
            .map(config =>
                new VarsUserServer(
                    config.internalUrl.toExternalForm,
                    config.timeout,
                    executor
                )
            )
