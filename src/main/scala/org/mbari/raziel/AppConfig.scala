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
import org.slf4j.LoggerFactory

/**
 * A typesafe wrapper around the application.conf file.
 * @author
 *   Brian Schlining
 */
object AppConfig:

  private val Default = "DEFAULT"

  private val config = ConfigFactory.load()

  private def asUrl(path: String): URL =
    if (!path.endsWith("/")) new URL(path)
    else new URL(path.substring(0, path.length - 1))

  val Name = "raziel"

  val Version = Try(getClass.getPackage.getImplementationVersion).getOrElse("0.0.0")

  val Description = "Configuration/Key Store"

  lazy val MasterKey = 
    val key = config.getString("raziel.master.key")
    if (key.trim.isEmpty || key.toUpperCase == Default)
      System.getLogger(getClass.getName)
        .log(System.Logger.Level.WARNING, "Using default master key. This is not recommended for production. Set the RAZIEL_MASTER_KEY environment variable to set a master key.")
    key

  val Annosaurus: EndpointConfig = 
    val url      = asUrl(config.getString("annosaurus.url"))
    val timeout  = config.getDuration("annosaurus.timeout")
    val secret   = config.getString("annosaurus.secret")
    EndpointConfig("annosaurus", url, timeout, Some(secret), "/anno")

  val Charybdis: EndpointConfig =
    val url      = asUrl(config.getString("charybdis.url"))
    val timeout  = config.getDuration("charybdis.timeout")
    EndpointConfig("charybdis", url, timeout, None, "/references")

  object Http:
    val Context     = config.getString("raziel.http.context")
    val Port        = config.getInt("raziel.http.port")
    val StopTimeout = config.getDuration("raziel.http.stop.timeout")
    val Webapp      = config.getString("raziel.http.webapp")

  object Jwt:
    val Expiration    = config.getDuration("raziel.jwt.expiration")
    val Issuer        = config.getString("raziel.jwt.issuer")
    lazy val SigningSecret = 
      val secret = config.getString("raziel.jwt.signing.secret")
      if (secret.trim.isEmpty || secret.toUpperCase == Default)
        System.getLogger(getClass.getName)
          .log(System.Logger.Level.WARNING, "Using default signing secret. This is not recommended for production. Set the RAZIEL_JWT_SIGNING_SECRET environment variable to set a signing secret.")
      secret

  val Panoptes: EndpointConfig = 
    val url      = asUrl(config.getString("panoptes.url"))
    val timeout  = config.getDuration("panoptes.timeout")
    val secret   = config.getString("panoptes.secret")
    EndpointConfig("panoptes", url, timeout, Some(secret), "/panoptes")

  val VampireSquid: EndpointConfig =
    val url      = asUrl(config.getString("vampire.squid.url"))
    val timeout  = config.getDuration("vampire.squid.timeout")
    val secret   = config.getString("vampire.squid.secret")
    EndpointConfig("vampire-squid", url, timeout, Some(secret), "/vam")

  val VarsKbServer: EndpointConfig =
    val url      = asUrl(config.getString("vars.kb.server.url"))
    val timeout  = config.getDuration("vars.kb.server.timeout")
    EndpointConfig("vars-kb-server", url, timeout, None, "/kb")
  
  val VarsUserServer: EndpointConfig =
    val url      = asUrl(config.getString("vars.user.server.url"))
    val timeout  = config.getDuration("vars.user.server.timeout")
    val secret   = config.getString("vars.user.server.secret")
    EndpointConfig("vars-user-server", url, timeout, Some(secret), "/accounts")
