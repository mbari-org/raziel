/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel

import com.typesafe.config.ConfigFactory
import java.net.URL
import org.mbari.raziel.domain.EndpointConfig

object AppConfig:
  private val config = ConfigFactory.load()

  private def asUrl(path: String): URL =
    if (path.endsWith("/")) new URL(path)
    else new URL(path + "/")

  object Annosaurus:
    val Url      = asUrl(config.getString("annosaurus.url"))
    val Timeout  = config.getDuration("annosaurus.timeout")
    val Secret   = config.getString("annosaurus.secret")
    val Endpoint = EndpointConfig("annosaurus", Url, Timeout, Some(Secret))

  object Charybdis:
    val Url      = asUrl(config.getString("charybdis.url"))
    val Timeout  = config.getDuration("charybdis.timeout")
    val Endpoint = EndpointConfig("charybdis", Url, Timeout, None)

  object Http:
    val Context     = config.getString("raziel.http.context")
    val Port        = config.getInt("raziel.http.port")
    val StopTimeout = config.getDuration("raziel.http.stop.timeout")
    val Webapp      = config.getString("raziel.http.webapp")

  object Jwt:
    val Expiration    = config.getDuration("raziel.jwt.expiration")
    val Issuer        = config.getString("raziel.jwt.issuer")
    val SigningSecret = config.getString("raziel.jwt.signing.secret")

  object VampireSquid:
    val Url      = asUrl(config.getString("vampire.squid.url"))
    val Timeout  = config.getDuration("vampire.squid.timeout")
    val Secret   = config.getString("vampire.squid.secret")
    val Endpoint = EndpointConfig("vampire-squid", Url, Timeout, Some(Secret))

  object Vars:
    object Kb:
      object Server:
        val Url      = asUrl(config.getString("vars.kb.server.url"))
        val Timeout  = config.getDuration("vars.kb.server.timeout")
        val Endpoint = EndpointConfig("vars-kb-server", Url, Timeout, None)
    object User:
      object Server:
        val Url      = asUrl(config.getString("vars.user.server.url"))
        val Timeout  = config.getDuration("vars.user.server.timeout")
        val Endpoint = EndpointConfig("vars-user-server", Url, Timeout, None)
