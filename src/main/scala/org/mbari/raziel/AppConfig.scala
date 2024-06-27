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
import org.mbari.raziel.etc.jdk.Logging.{given, *}
import java.net.URI

/**
 * A typesafe wrapper around the application.conf file.
 * @author
 *   Brian Schlining
 */
object AppConfig:

  private val log = System.getLogger(getClass.getName)

  private val Default = "DEFAULT"

  private val config = ConfigFactory.load()

  private def asUrl(path: String): URL =
    if (!path.endsWith("/")) URI.create(path).toURL()
    else URI.create(path.substring(0, path.length - 1)).toURL()

  val Name = "raziel"

  val Version = Try(getClass.getPackage.getImplementationVersion).getOrElse("0.0.0")

  val Description = "Configuration/Key Store"

  lazy val MasterKey =
    val key = config.getString("raziel.master.key")
    if (key.trim.isEmpty || key.toUpperCase == Default)
      log
        .atWarn
        .log(
          "Using default master key. This is not recommended for production. Set the RAZIEL_MASTER_KEY environment variable to set a master key."
        )
    key

  lazy val Annosaurus: EndpointConfig =
    val url         = asUrl(config.getString("annosaurus.url"))
    val timeout     = config.getDuration("annosaurus.timeout")
    val secret      = config.getString("annosaurus.secret")
    val internalUrl = asUrl(config.getString("annosaurus.internal.url"))
    log.atDebug.log(s"Annosaurus URL: $url")
    EndpointConfig("annosaurus", url, timeout, Some(secret), "/anno", internalUrl)

  lazy val Beholder: EndpointConfig =
    val url         = asUrl(config.getString("beholder.url"))
    val timeout     = config.getDuration("beholder.timeout")
    val secret      = config.getString("beholder.secret")
    val internalUrl = asUrl(config.getString("beholder.internal.url"))
    log.atDebug.log(s"Beholder URL: $url")
    EndpointConfig("beholder", url, timeout, Some(secret), "/beholder", internalUrl)

  lazy val Charybdis: EndpointConfig =
    val url         = asUrl(config.getString("charybdis.url"))
    val timeout     = config.getDuration("charybdis.timeout")
    val internalUrl = asUrl(config.getString("charybdis.internal.url"))
    log.atDebug.log(s"Charybdis URL: $url")
    EndpointConfig("charybdis", url, timeout, None, "/references", internalUrl)

  object Http:
    val Context     = config.getString("raziel.http.context")
    val Port        = config.getInt("raziel.http.port")
    val StopTimeout = config.getDuration("raziel.http.stop.timeout")
    val Webapp      = config.getString("raziel.http.webapp")

  object Jwt:
    val Expiration         = config.getDuration("raziel.jwt.expiration")
    val Issuer             = config.getString("raziel.jwt.issuer")
    lazy val SigningSecret =
      val secret = config.getString("raziel.jwt.signing.secret")
      if (secret.trim.isEmpty || secret.toUpperCase == Default)
        System
          .getLogger(getClass.getName)
          .log(
            System.Logger.Level.WARNING,
            "Using default signing secret. This is not recommended for production. Set the RAZIEL_JWT_SIGNING_SECRET environment variable to set a signing secret."
          )
      secret

  // TODO - all of these should return an Option[EndpointConfig] and log a warning if the URL is not set
  lazy val Oni: EndpointConfig =
    val url         = asUrl(config.getString("oni.url"))
    val timeout     = config.getDuration("oni.timeout")
    val secret      = config.getString("oni.secret")
    val internalUrl = asUrl(config.getString("oni.internal.url"))
    log.atDebug.log(s"Oni URL: $url")
    EndpointConfig("oni", url, timeout, Some(secret), "/oni", internalUrl)

  lazy val Panoptes: EndpointConfig =
    val url         = asUrl(config.getString("panoptes.url"))
    val timeout     = config.getDuration("panoptes.timeout")
    val secret      = config.getString("panoptes.secret")
    val internalUrl = asUrl(config.getString("panoptes.internal.url"))
    log.atDebug.log(s"Panoptes URL: $url")
    EndpointConfig("panoptes", url, timeout, Some(secret), "/panoptes", internalUrl)

  lazy val VampireSquid: EndpointConfig =
    val url         = asUrl(config.getString("vampire.squid.url"))
    val timeout     = config.getDuration("vampire.squid.timeout")
    val secret      = config.getString("vampire.squid.secret")
    val internalUrl = asUrl(config.getString("vampire.squid.internal.url"))
    log.atDebug.log(s"Vampire-squid URL: $url")
    EndpointConfig("vampire-squid", url, timeout, Some(secret), "/vam", internalUrl)

  lazy val VarsKbServer: EndpointConfig =
    val url         = asUrl(config.getString("vars.kb.server.url"))
    val timeout     = config.getDuration("vars.kb.server.timeout")
    val internalUrl = asUrl(config.getString("vars.kb.server.internal.url"))
    log.atDebug.log(s"VARS KB Server URL: $url")
    EndpointConfig("vars-kb-server", url, timeout, None, "/kb", internalUrl)

  lazy val VarsUserServer: EndpointConfig =
    val url         = asUrl(config.getString("vars.user.server.url"))
    val timeout     = config.getDuration("vars.user.server.timeout")
    val secret      = config.getString("vars.user.server.secret")
    val internalUrl = asUrl(config.getString("vars.user.server.internal.url"))
    log.atDebug.log(s"VARS User Server URL: $url")
    EndpointConfig("vars-user-server", url, timeout, Some(secret), "/accounts", internalUrl)

