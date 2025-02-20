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
        // try
        //     val url         = asUrl(config.getString("annosaurus.url"))
        //     val timeout     = config.getDuration("annosaurus.timeout")
        //     val secret      = config.getString("annosaurus.secret")
        //     val internalUrl = asUrl(config.getString("annosaurus.internal.url"))
        //     log.atDebug.log(s"Annosaurus URL: $url")
        //     Some(EndpointConfig(AnnosaurusName, url, timeout, Some(secret), "/anno", internalUrl))
        // catch
        //     case NonFatal(e) =>
        //         log.atWarn
        //             .withCause(e)
        //             .log("Annosaurus is not configured. Raziel will not be able to access the annotation service.")
        //         None

    val BeholderName: String                  = "beholder"
    lazy val Beholder: Option[EndpointConfig] = buildEndpointConfig(
        BeholderName,
        "beholder",
        hasSecret = true,
        proxyPath = "/beholder"
    )
        // try
        //     val url         = asUrl(config.getString("beholder.url"))
        //     val timeout     = config.getDuration("beholder.timeout")
        //     val secret      = config.getString("beholder.secret")
        //     val internalUrl = asUrl(config.getString("beholder.internal.url"))
        //     log.atDebug.log(s"Beholder URL: $url")
        //     Some(EndpointConfig(BeholderName, url, timeout, Some(secret), "/beholder", internalUrl))
        // catch
        //     case NonFatal(e) =>
        //         log.atWarn
        //             .withCause(e)
        //             .log("Beholder is not configured. Raziel will not be able to access the image capture service.")
        //         None

    lazy val CharybdisName: String             = "charybdis"
    lazy val Charybdis: Option[EndpointConfig] = buildEndpointConfig(
        CharybdisName,
        "charybdis",
        hasSecret = false,
        proxyPath = "/references"
    )
        // try
        //     val url         = asUrl(config.getString("charybdis.url"))
        //     val timeout     = config.getDuration("charybdis.timeout")
        //     val internalUrl = asUrl(config.getString("charybdis.internal.url"))
        //     log.atDebug.log(s"Charybdis URL: $url")
        //     Some(EndpointConfig(CharybdisName, url, timeout, None, "/references", internalUrl))
        // catch
        //     case NonFatal(e) =>
        //         log.atWarn
        //             .withCause(e)
        //             .log("Charybdis is not configured. Raziel will not be able to access the reference service.")
        //         None

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
        // try
        //     val url         = asUrl(config.getString("oni.url"))
        //     val timeout     = config.getDuration("oni.timeout")
        //     val secret      = config.getString("oni.secret")
        //     val internalUrl = asUrl(config.getString("oni.internal.url"))
        //     log.atDebug.log(s"Oni URL: $url")
        //     Some(EndpointConfig(OniName, url, timeout, Some(secret), "/oni", internalUrl))
        // catch
        //     case NonFatal(e) =>
        //         log.atInfo.withCause(e).log("Oni is not configured. Writing to the VARS KB will not be possible.")
        //         None

    lazy val PanoptesName: String             = "panoptes"
    lazy val Panoptes: Option[EndpointConfig] = buildEndpointConfig(
        PanoptesName,
        "panoptes",
        hasSecret = true,
        proxyPath = "/panoptes"
    )
        // try
        //     val url         = asUrl(config.getString("panoptes.url"))
        //     val timeout     = config.getDuration("panoptes.timeout")
        //     val secret      = config.getString("panoptes.secret")
        //     val internalUrl = asUrl(config.getString("panoptes.internal.url"))
        //     log.atDebug.log(s"Panoptes URL: $url")
        //     Some(EndpointConfig(PanoptesName, url, timeout, Some(secret), "/panoptes", internalUrl))
        // catch
        //     case NonFatal(e) =>
        //         log.atWarn
        //             .withCause(e)
        //             .log("Panoptes is not configured. Raziel will not be able to access the image archiving service.")
        //         None

    lazy val SkimmerName: String             = "skimmer"
    lazy val Skimmer: Option[EndpointConfig] = buildEndpointConfig(
        SkimmerName,
        "skimmer",
        hasSecret = false,
        proxyPath = "/skimmer"
    )
        // try
        //     val url         = asUrl(config.getString("skimmer.url"))
        //     val timeout     = config.getDuration("skimmer.timeout")
        //     val internalUrl = asUrl(config.getString("skimmer.internal.url"))
        //     log.atDebug.log(s"Skimmer URL: $url")
        //     Some(EndpointConfig(SkimmerName, url, timeout, None, "/skimmer", internalUrl))
        // catch
        //     case NonFatal(e) =>
        //         log.atWarn
        //             .withCause(e)
        //             .log("Skimmer is not configured. Raziel will not be able to access the image processing service.")
        //         None

    lazy val VampireSquidName: String             = "vampire-squid"
    lazy val VampireSquid: Option[EndpointConfig] = buildEndpointConfig(
        VampireSquidName,
        "vampire.squid",
        hasSecret = true,
        proxyPath = "/vam"
    )
        // try
        //     val url         = asUrl(config.getString("vampire.squid.url"))
        //     val timeout     = config.getDuration("vampire.squid.timeout")
        //     val secret      = config.getString("vampire.squid.secret")
        //     val internalUrl = asUrl(config.getString("vampire.squid.internal.url"))
        //     log.atDebug.log(s"Vampire-squid URL: $url")
        //     Some(EndpointConfig(VampireSquidName, url, timeout, Some(secret), "/vam", internalUrl))
        // catch
        //     case NonFatal(e) =>
        //         log.atWarn
        //             .withCause(e)
        //             .log(
        //                 "Vampire-squid is not configured. Raziel will not be able to access the video asset manager service."
        //             )
        //         None

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
                // try
                //     val url         = asUrl(config.getString("vars.kb.server.url"))
                //     val timeout     = config.getDuration("vars.kb.server.timeout")
                //     val internalUrl = asUrl(config.getString("vars.kb.server.internal.url"))
                //     log.atDebug.log(s"VARS KB Server URL: $url")
                //     Some(EndpointConfig(VarsKbServerName, url, timeout, None, "/kb", internalUrl))
                // catch
                //     case NonFatal(e) =>
                //         log.atInfo
                //             .withCause(e)
                //             .log(s"$VarsKbServerName is not configured. Access to the VARS KB will not be possible.")
                //         None

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
                // try
                //     val url         = asUrl(config.getString("vars.user.server.url"))
                //     val timeout     = config.getDuration("vars.user.server.timeout")
                //     val secret      = config.getString("vars.user.server.secret")
                //     val internalUrl = asUrl(config.getString("vars.user.server.internal.url"))
                //     log.atDebug.log(s"VARS User Server URL: $url")
                //     Some(EndpointConfig(VarsUserServerName, url, timeout, Some(secret), "/accounts", internalUrl))
                // catch
                //     case NonFatal(e) =>
                //         log.atInfo
                //             .withCause(e)
                //             .log(
                //                 s"$VarsUserServerName is not configured. Raziel will not function correctly with a user service configured."
                //             )
                //         None
