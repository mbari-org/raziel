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

package org.mbari.raziel.etc.methanol

import com.github.mizosoft.methanol.Methanol
import io.circe.Decoder
import io.circe.Json
import io.circe.parser.{decode, parse}
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.concurrent.{Executor, Executors}
import org.mbari.raziel.AppConfig
import org.mbari.raziel.ext.methanol.LoggingInterceptor
import scala.util.Try

/**
 * Helper for using javas' HttpClient.
 * @author
 *   Brian Schlining
 */
class HttpClientSupport(
    timeout: Duration = Duration.ofSeconds(10),
    executor: Executor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)
):

    val client: Methanol = Methanol
        .newBuilder()
        .autoAcceptEncoding(true)
        .connectTimeout(timeout)
        .executor(executor)
        .interceptor(LoggingInterceptor)
        .readTimeout(timeout)
        .requestTimeout(timeout)
        .userAgent(AppConfig.Name)
        .build()

    def requestObjects[T: Decoder](request: HttpRequest): Either[Throwable, T] =
        for
            body <- requestString(request)
            obj  <- decode[T](body)
        yield obj

    def requestString(request: HttpRequest): Either[Throwable, String] =
        for
            response <- Try(client.send(request, BodyHandlers.ofString())).toEither
            body     <- if response.statusCode() == 200 then Right(response.body)
                        else
                            Left(
                                new RuntimeException(s"Unexpected response from ${request.uri}: ${response.body}")
                            )
        yield body
