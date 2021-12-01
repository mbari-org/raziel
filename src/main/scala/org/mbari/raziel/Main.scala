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

import java.util.concurrent.Callable
import java.util.logging.Handler
import org.eclipse.jetty.proxy.ProxyServlet
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletHolder}
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.webapp.WebAppContext
import org.mbari.raziel.domain.EndpointConfig
import org.scalatra.servlet.ScalatraListener
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.{Command, Option => Opt, Parameters}
import scala.util.Try

@Command(
  description = Array("Start the server"),
  name = "main",
  mixinStandardHelpOptions = true,
  version = Array("0.0.1")
)
class MainRunner extends Callable[Int]:

  @Parameters(index = "0", description = Array("The port of the server. default: $DEFAULT_VALUE"))
  private var port: Int = AppConfig.Http.Port

  override def call(): Int =
    Main.run(port) match
      case Left(_)  => -1
      case Right(_) => 0

object Main:

  def main(args: Array[String]): Unit =
    new CommandLine(new MainRunner()).execute(args: _*)

  def run(port: Int): Either[Throwable, Unit] = Try {

    val s = """
      |  ______ _______ ______ _____ _______       
      | |_____/ |_____|  ____/   |   |______ |     
      | |    \_ |     | /_____ __|__ |______ |_____""".stripMargin

    println(s)

    val server: Server = new Server(port)
    server.setStopAtShutdown(true)

    val context = new WebAppContext
    context.setContextPath(AppConfig.Http.Context)
    context.setResourceBase(AppConfig.Http.Webapp)
    context.addEventListener(ScalatraListener())

    val log       = LoggerFactory.getLogger(getClass)
    // Setup proxies for all endpoints
    val endpoints = EndpointConfig.defaults
    for (e, i) <- endpoints.zipWithIndex do
      var proxy = new ServletHolder(classOf[ProxyServlet.Transparent])
      proxy.setInitParameter("proxyTo", e.url.toExternalForm)
      proxy.setInitParameter("prefix", s"${e.proxyPath}")
      context.addServlet(proxy, s"${e.proxyPath}/*")
      log
        .atInfo
        .log(() => s"Proxying ${e.name} @ ${e.url.toExternalForm} as ${e.proxyPath}")

    server.setHandler(context)
    server.start()

    log
      .atInfo
      .log(() => s"Started Raziel on port $port")

    server.join()
  }.toEither
