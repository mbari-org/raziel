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

package org.mbari.raziel.domain

import java.net.URL
import java.time.Duration
import org.mbari.raziel.AppConfig

case class EndpointConfig(
    name: String,
    url: URL,
    timeout: Duration,
    secret: Option[String],
    proxyPath: String
)

object EndpointConfig:

  def defaults: List[EndpointConfig] =
    AppConfig.Annosaurus.Endpoint ::
      AppConfig.Charybdis.Endpoint ::
      AppConfig.Panoptes.Endpoint ::
      AppConfig.VampireSquid.Endpoint ::
      AppConfig.Vars.Kb.Server.Endpoint ::
      AppConfig.Vars.User.Server.Endpoint ::
      Nil
