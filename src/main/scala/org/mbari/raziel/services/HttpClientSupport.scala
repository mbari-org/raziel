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
import io.circe.Decoder
import io.circe.parser.decode
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.concurrent.{Executor, Executors}
import org.mbari.raziel.AppConfig
import org.mbari.raziel.ext.methanol.LoggingInterceptor
import zio.Task

/**
 * Helper for using javas' HttpClient.
 * @author
 *   Brian Schlining
 */
class HttpClientSupport(
    timeout: Duration = Duration.ofSeconds(10),
    executor: Executor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)
):

  private val client = Methanol
    .newBuilder()
    .autoAcceptEncoding(true)
    .connectTimeout(timeout)
    .executor(executor)
    .interceptor(LoggingInterceptor)
    .readTimeout(timeout)
    .requestTimeout(timeout)
    .userAgent(AppConfig.Name)
    .build()

  /**
   * Convert a [[HttpRequest]] to a [[Task]]. Used to compose IO.
   * @param request
   *   The request to convert to a zio Task
   * @tparam T
   *   The type of the response body. Requires an implicit circe [[Decoder]] is in scope.
   * @return
   *   A [[Task]] that will resolve to the response body
   */
  def requestToTask[T](
      request: HttpRequest
  )(implicit decoder: Decoder[T]): Task[T] = zio.Task.effect {
    val response = client.send(request, BodyHandlers.ofString())
    if (response.statusCode() == 200)
      val json = response.body()
      decode[T](json) match
        case Right(i) => i
        case Left(_)  =>
          val msg = s"Unexpected response from ${request.uri}: $json"
          throw new RuntimeException(msg)
    else throw new RuntimeException(s"Response was ${response.body()}: ${response.statusCode()}")
  }
