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
