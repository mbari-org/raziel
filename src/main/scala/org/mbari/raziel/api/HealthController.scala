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

package org.mbari.raziel.api

import org.mbari.raziel.domain.HealthStatus
import org.mbari.raziel.domain.ServiceStatus
import org.mbari.raziel.services.HealthService
import org.mbari.raziel.services.HealthServices
import org.mbari.raziel.domain.ServerError
import org.mbari.raziel.domain.ErrorMsg
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.mbari.raziel.etc.zio.ZioUtil

class HealthController(services: Seq[HealthService]):

  private val healthService = HealthServices(services)

  def defaultHealthStatus: HealthStatus = HealthStatus.default

  def expectedServiceStatus: Seq[ServiceStatus] = services.map(s => ServiceStatus(s.name))

  def availableServiceStatus(): Either[ErrorMsg, Seq[ServiceStatus]] =
    val app =
      for healthStatuses <- healthService.fetchHealth()
      yield healthStatuses
        .filter(_.freeMemory > 0)
        .map(hs => ServiceStatus(hs.application, Some(hs)))

    Try(ZioUtil.unsafeRun(app)) match
      case Success(s) => Right(s)
      case Failure(e) =>
        Left(ServerError(e.getMessage))

  def currentServiceStatus(): Either[ErrorMsg, Seq[ServiceStatus]] =
    val app =
      for healthStatuses <- healthService.fetchHealth()
      yield healthStatuses
        .map(hs =>
          val ss = if (hs.freeMemory <= 0) None else Some(hs)
          ServiceStatus(hs.application, ss)
        )

    Try(ZioUtil.unsafeRun(app)) match
      case Success(s) => Right(s)
      case Failure(e) =>
        Left(ServerError(e.getMessage))
