/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.domain

import org.mbari.raziel.AppConfig
import java.net.URL
import java.time.Duration

case class EndpointConfig(name: String, url: URL, timeout: Duration, secret: Option[String])

object EndpointConfig:

  def defaults: List[EndpointConfig] =
    AppConfig.Annosaurus.Endpoint ::
      AppConfig.Charybdis.Endpoint ::
      AppConfig.VampireSquid.Endpoint ::
      AppConfig.Vars.Kb.Server.Endpoint ::
      AppConfig.Vars.User.Server.Endpoint ::
      Nil
