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

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import scala.concurrent.Future
import org.mbari.raziel.AppConfig

case class SwaggerEndpoints(
    authEndpoints: AuthEndpoints,
    endpointsEndpoints: EndpointsEndpoints,
    healthEndpoints: HealthEndpoints
):

    val allImpl: List[ServerEndpoint[Any, Future]] =
        SwaggerInterpreter()
            .fromEndpoints[Future](
                authEndpoints.all ++ endpointsEndpoints.all ++ healthEndpoints.all,
                AppConfig.Name,
                AppConfig.Version
            )
