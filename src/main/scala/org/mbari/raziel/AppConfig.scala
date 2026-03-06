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
import org.mbari.raziel.etc.jdk.Logging.{*, given}

import java.net.URI
import scala.util.control.NonFatal
import com.typesafe.config.Config

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
        if !path.endsWith("/") then URI.create(path).toURL()
        else URI.create(path.substring(0, path.length - 1)).toURL()

    val Name: String = "raziel"

    val Version: String = Try(getClass.getPackage.getImplementationVersion).getOrElse("0.0.0")

    val Description = "Configuration/Key Store"

    def buildEndpointConfig(
        name: String,
        nodePrefix: String,
        hasSecret: Boolean,
        proxyPath: String
    ): Option[EndpointConfig] =
        try
            val endpointConfig = config.getConfig(nodePrefix)
            val url         = asUrl(endpointConfig.getString("url"))
            val timeout     = endpointConfig.getDuration("timeout")
            val secret      = if hasSecret then Option(endpointConfig.getString("secret")) else None
            val internalUrl = asUrl(endpointConfig.getString("internal.url"))
            log.atDebug.log(s"Endpoint URL: $url")
            Some(EndpointConfig(name, url, timeout, secret, proxyPath, internalUrl))
        catch
            case NonFatal(e) =>
                log.atWarn
                    .withCause(e)
                    .log(s"$name is not configured. Raziel will not be able to access the service.")
                None

    lazy val MasterKey: String =
        val key = config.getString("raziel.master.key")
        if key.trim.isEmpty || key.toUpperCase == Default then
            log
                .atWarn
                .log(
                    "Using default master key. This is not recommended for production. Set the RAZIEL_MASTER_KEY environment variable to set a master key."
                )
        key

    val AnnosaurusName: String                  = "annosaurus"
    lazy val Annosaurus: Option[EndpointConfig] = buildEndpointConfig(
        AnnosaurusName,
        "annosaurus",
        hasSecret = true,
        proxyPath = "/anno"
    )


    val BeholderName: String                  = "beholder"
    lazy val Beholder: Option[EndpointConfig] = buildEndpointConfig(
        BeholderName,
        "beholder",
        hasSecret = true,
        proxyPath = "/beholder"
    )


    lazy val CharybdisName: String             = "charybdis"
    lazy val Charybdis: Option[EndpointConfig] = buildEndpointConfig(
        CharybdisName,
        "charybdis",
        hasSecret = false,
        proxyPath = "/references"
    )


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
            if secret.trim.isEmpty || secret.toUpperCase == Default then
                System
                    .getLogger(getClass.getName)
                    .log(
                        System.Logger.Level.WARNING,
                        "Using default signing secret. This is not recommended for production. Set the RAZIEL_JWT_SIGNING_SECRET environment variable to set a signing secret."
                    )
            secret

    lazy val OniName: String             = "oni"
    lazy val Oni: Option[EndpointConfig] = buildEndpointConfig(
        OniName,
        "oni",
        hasSecret = true,
        proxyPath = "/oni"
    )

    lazy val PanoptesName: String             = "panoptes"
    lazy val Panoptes: Option[EndpointConfig] = buildEndpointConfig(
        PanoptesName,
        "panoptes",
        hasSecret = true,
        proxyPath = "/panoptes"
    )


    lazy val SkimmerName: String             = "skimmer"
    lazy val Skimmer: Option[EndpointConfig] = buildEndpointConfig(
        SkimmerName,
        "skimmer",
        hasSecret = false,
        proxyPath = "/skimmer"
    )

    lazy val VampireSquidName: String             = "vampire-squid"
    lazy val VampireSquid: Option[EndpointConfig] = buildEndpointConfig(
        VampireSquidName,
        "vampire.squid",
        hasSecret = true,
        proxyPath = "/vam"
    )


    lazy val VarsKbServerName: String             = "vars-kb-server"
    lazy val VarsKbServer: Option[EndpointConfig] =
        Oni match
            case Some(oni) =>
                log.atInfo.log(s"Using $OniName instead of $VarsKbServerName for the VARS knowledgebase")
                Some(oni.copy(name = VarsKbServerName))
            case None      => buildEndpointConfig(
                VarsKbServerName,
                "vars.kb.server",
                hasSecret = false,
                proxyPath = "/kb"
            )


    lazy val VarsUserServerName: String             = "vars-user-server"
    lazy val VarsUserServer: Option[EndpointConfig] =
        Oni match
            case Some(oni) =>
                log.atInfo.log(s"Using $OniName instead of $VarsUserServerName for VARS user services.")
                Some(oni.copy(name = VarsUserServerName))
            case None      =>
                buildEndpointConfig(
                    VarsUserServerName,
                    "vars.user.server",
                    hasSecret = true,
                    proxyPath = "/accounts"
                )
