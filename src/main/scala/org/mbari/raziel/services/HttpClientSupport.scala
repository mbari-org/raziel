/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.services

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.Executor
import com.github.mizosoft.methanol.Methanol
import org.mbari.raziel.ext.methanol.LoggingInterceptor
import java.net.http.HttpRequest
import io.circe.Decoder
import zio.Task
import java.net.http.HttpResponse.BodyHandlers
import io.circe.parser.decode

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
    .userAgent("Raziel")
    .build()

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
