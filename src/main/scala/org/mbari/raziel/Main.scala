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

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import java.lang.System.Logger.Level
import java.util.concurrent.Callable
import java.util.logging.Handler
import org.mbari.raziel.api.*
import org.mbari.raziel.domain.EndpointConfig
import org.mbari.raziel.etc.jdk.Logging.given
import org.mbari.raziel.services.*
import picocli.CommandLine
import picocli.CommandLine.{Command, Option => Opt, Parameters}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.util.Try
import sttp.tapir.server.vertx.VertxFutureServerInterpreter
import sttp.tapir.server.vertx.VertxFutureServerInterpreter.*
import io.vertx.ext.web.handler.CorsHandler

@Command(
  description = Array("Start the server"),
  name = "main",
  mixinStandardHelpOptions = true,
  version = Array("0.0.1")
)
class MainRunner extends Callable[Int]:

  @Opt(
    names = Array("-p", "--port"),
    description = Array("The port of the server. default: ${DEFAULT-VALUE}")
  )
  private var port: Int = AppConfig.Http.Port

  override def call(): Int =
    Main.run(port)
    0

object Main:

  private val log = System.getLogger(getClass.getName)

  def main(args: Array[String]): Unit =
    val s = """
      |  ______ _______ ______ _____ _______       
      | |_____/ |_____|  ____/   |   |______ |     
      | |    \_ |     | /_____ __|__ |______ |_____""".stripMargin
    println(s)
    new CommandLine(new MainRunner()).execute(args: _*)

  def run(port: Int): Unit =
    log.atInfo.log(s"Starting up ${AppConfig.Name} v${AppConfig.Version} on port $port")

    given executionContext: ExecutionContextExecutor = ExecutionContext.global

    // -- Service providers
    val annosaurus     = Annosaurus.default
    val beholder       = Beholder.default
    val charybdis      = Charybdis.default
    val panoptes       = Panoptes.default
    val vampireSquid   = VampireSquid.default
    val varsKbServer   = VarsKbServer.default
    val varsUserServer = VarsUserServer.default
    val healthServices =
      Seq(annosaurus, beholder, charybdis, panoptes, vampireSquid, varsKbServer, varsUserServer)

    // -- Tapir endpoints
    val context           = AppConfig.Http.Context
    val authEndpoints     = AuthEndpoints(AuthController(varsUserServer), context)
    val healthEndpoints   = HealthEndpoints(HealthController(healthServices), context)
    val endpointEndpoints = EndpointsEndpoints(context)
    val swaggerEndpoints  = SwaggerEndpoints(authEndpoints, endpointEndpoints, healthEndpoints)
    val allEndpointImpls  = authEndpoints.allImpl ++
      healthEndpoints.allImpl ++
      endpointEndpoints.allImpl ++
      swaggerEndpoints.allImpl

    // -- Vert.x server
    val vertx  = Vertx.vertx()
    val server = vertx.createHttpServer()
    val router = Router.router(vertx)

    // Add CORS
    val corsHandler = CorsHandler.create("*")
    router.route().handler(corsHandler)

    // Add Tapir endpoints
    val interpreter = VertxFutureServerInterpreter()
    authEndpoints.allImpl.foreach(endpoint => interpreter.blockingRoute(endpoint).apply(router))
    healthEndpoints.allImpl.foreach(endpoint => interpreter.blockingRoute(endpoint).apply(router))
    endpointEndpoints.allImpl.foreach(endpoint => interpreter.route(endpoint).apply(router))
    swaggerEndpoints.allImpl.foreach(endpoint => interpreter.route(endpoint).apply(router))


//    for endpoint <- allEndpointImpls do
//      val attach = VertxFutureServerInterpreter().route(endpoint)
//      attach(router)

    Await.result(server.requestHandler(router).listen(port).asScala, Duration.Inf)
