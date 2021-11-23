/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel

import java.util.concurrent.Callable
import picocli.CommandLine
import picocli.CommandLine.{Command, Option => Opt, Parameters}
import scala.util.Try
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.NetworkTrafficServerConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.HttpConnectionFactory
import org.scalatra.servlet.ScalatraListener

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
    Main.run(port)
    0

object Main:

  def main(args: Array[String]): Unit =
    new CommandLine(new MainRunner()).execute(args: _*)

  def run(port: Int): Either[Throwable, Unit] = Try {
    val server: Server = new Server
    server.setStopAtShutdown(true)

    val httpConfig = new HttpConfiguration()
    httpConfig.setSendDateHeader(true)
    httpConfig.setSendServerVersion(false)

    val connector = new NetworkTrafficServerConnector(
      server,
      new HttpConnectionFactory(httpConfig)
    )
    connector.setPort(port)
    println(s"Starting Scalatra on port $port")
    connector.setIdleTimeout(90000)
    server.addConnector(connector)

    val webApp = new WebAppContext
    webApp.setContextPath(AppConfig.Http.Context)
    webApp.setResourceBase(AppConfig.Http.Webapp)
    webApp.setEventListeners(Array(new ScalatraListener))

    server.setHandler(webApp)

    server.start()
  }.toEither
