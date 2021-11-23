/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.ext.methanol

import com.github.mizosoft.methanol.Methanol
import com.github.mizosoft.methanol.Methanol.Interceptor.Chain
import org.slf4j.LoggerFactory

import java.net.http.{HttpHeaders, HttpRequest, HttpResponse}
import java.time.{Duration, Instant}
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

object LoggingInterceptor extends Methanol.Interceptor:

  private val log = LoggerFactory.getLogger(getClass)

  override def intercept[T](request: HttpRequest, chain: Chain[T]): HttpResponse[T] =
    logRequest(request)
    toLoggingChain(request, chain).forward(request)

  override def interceptAsync[T](
      request: HttpRequest,
      chain: Chain[T]
  ): CompletableFuture[HttpResponse[T]] =
    logRequest(request)
    toLoggingChain(request, chain).forwardAsync(request)

  private def logRequest(request: HttpRequest): Unit =
    log
      .atDebug()
      .log(() => s""" Sent >>>
        |${request.method()} ${request.uri()}
        |${headersToString(request.headers())}""".stripMargin.trim())

  private def toLoggingChain[T](request: HttpRequest, chain: Chain[T]): Chain[T] =

    val sentAt = Instant.now()
    // format: off
    chain.withBodyHandler(responseInfo =>
      log
        .atDebug()
        .log(() =>
          s""" Received <<< ${request.method()} ${request.uri()} in ${Duration.between(sentAt, Instant.now()).toMillis()}ms
          |${responseInfo.statusCode()}
          |${headersToString(responseInfo.headers())}""".stripMargin.trim()
        )
      chain.bodyHandler().apply(responseInfo)
    )
    // format: on

  private def headersToString(headers: HttpHeaders): String =
    headers
      .map()
      .entrySet()
      .stream()
      .map(e => s"${e.getKey()}: ${String.join(", ", e.getValue())}")
      .collect(Collectors.joining(System.lineSeparator()))
      .trim()
