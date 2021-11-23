/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

import javax.servlet.ServletContext

import scala.concurrent.ExecutionContext
import org.scalatra.LifeCycle
import scala.concurrent.ExecutionContextExecutor
import org.mbari.raziel.AppConfig
import org.mbari.raziel.services.VarsUserServer
import org.mbari.raziel.api.{AuthApi, EndpointsApi}

class ScalatraBootstrap extends LifeCycle:

  override def init(context: ServletContext): Unit =

    println("STARTING UP NOW")
    // Optional because * is the default
    context.setInitParameter("org.scalatra.cors.allowedOrigins", "*")
    // Disables cookies, but required because browsers will not allow passing credentials to wildcard domains
    context.setInitParameter("org.scalatra.cors.allowCredentials", "false")
    context.setInitParameter(
      "org.scalatra.cors.allowedMethods",
      "GET, POST, ORIGIN"
    )

    // Methonal needs a Java executor. Everything else a Scala ExeuctionContext
    given executionContext: ExecutionContextExecutor = ExecutionContext.global

    val varsUserServer = VarsUserServer.default

    context.mount(AuthApi(varsUserServer), "/auth")
    context.mount(EndpointsApi(), "/endpoints")
