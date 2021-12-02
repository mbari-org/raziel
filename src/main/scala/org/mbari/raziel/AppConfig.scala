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

package org.mbari.raziel

import com.typesafe.config.ConfigFactory
import java.net.URL
import org.mbari.raziel.domain.EndpointConfig
import scala.util.Try

/**
 * A typesafe wrapper around the application.conf file.
 * @author
 *   Brian Schlining
 */
object AppConfig:

  val Name = "raziel"

  private val config = ConfigFactory.load()

  private def asUrl(path: String): URL =
    if (!path.endsWith("/")) new URL(path)
    else new URL(path.substring(0, path.length - 1))

  val Version = Try(getClass.getPackage.getImplementationVersion).getOrElse("0.0.0")

  object Annosaurus:
    val Url      = asUrl(config.getString("annosaurus.url"))
    val Timeout  = config.getDuration("annosaurus.timeout")
    val Secret   = config.getString("annosaurus.secret")
    val Endpoint = EndpointConfig("annosaurus", Url, Timeout, Some(Secret), "/anno")

  object Charybdis:
    val Url      = asUrl(config.getString("charybdis.url"))
    val Timeout  = config.getDuration("charybdis.timeout")
    val Endpoint = EndpointConfig("charybdis", Url, Timeout, None, "/references")

  object Http:
    val Context     = config.getString("raziel.http.context")
    val Port        = config.getInt("raziel.http.port")
    val StopTimeout = config.getDuration("raziel.http.stop.timeout")
    val Webapp      = config.getString("raziel.http.webapp")

  object Jwt:
    val Expiration    = config.getDuration("raziel.jwt.expiration")
    val Issuer        = config.getString("raziel.jwt.issuer")
    val SigningSecret = config.getString("raziel.jwt.signing.secret")

  object Panoptes:
    val Url      = asUrl(config.getString("panoptes.url"))
    val Timeout  = config.getDuration("panoptes.timeout")
    val Secret   = config.getString("panoptes.secret")
    val Endpoint = EndpointConfig("panoptes", Url, Timeout, Some(Secret), "/panoptes")

  object VampireSquid:
    val Url      = asUrl(config.getString("vampire.squid.url"))
    val Timeout  = config.getDuration("vampire.squid.timeout")
    val Secret   = config.getString("vampire.squid.secret")
    val Endpoint = EndpointConfig("vampire-squid", Url, Timeout, Some(Secret), "/vam")

  object VarsKbServer:
    val Url      = asUrl(config.getString("vars.kb.server.url"))
    val Timeout  = config.getDuration("vars.kb.server.timeout")
    val Endpoint = EndpointConfig("vars-kb-server", Url, Timeout, None, "/kb")
  
  object VarsUserServer:
    val Url      = asUrl(config.getString("vars.user.server.url"))
    val Timeout  = config.getDuration("vars.user.server.timeout")
    val Endpoint = EndpointConfig("vars-user-server", Url, Timeout, None, "/accounts")
