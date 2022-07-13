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

package org.mbari.raziel.api

import io.circe.generic.auto._
import org.mbari.raziel.domain.{BearerAuth, ErrorMsg}
import org.mbari.raziel.etc.circe.CirceCodecs.given
import scala.concurrent.{ExecutionContext, Future}
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

class AuthEndpoints(authController: AuthController)(using ec: ExecutionContext) extends Endpoints:

  val authEndpoint: PublicEndpoint[(Option[String], Option[String]), ErrorMsg, BearerAuth, Any] =
    baseEndpoint
      .post
      .in("auth")
      .in(header[Option[String]]("X-Api-Key"))
      .in(header[Option[String]]("Authorization"))
      .out(jsonBody[BearerAuth])
      .description("Authenticate with API key")
  val authEndpointImpl: ServerEndpoint[Any, Future]                                             =
    authEndpoint
      .serverLogic((xApiKeyOpt, authOpt) =>
        Future(authController.authenticate(xApiKeyOpt, authOpt))
      )

  val verifyEndpoint: PublicEndpoint[Option[String], ErrorMsg, Map[String, String], Any] =
    baseEndpoint
      .post
      .in("auth" / "verify")
      .in(header[Option[String]]("Authorization"))
      .out(jsonBody[Map[String, String]])
      .description("Verify the user's JWT token")
  val verifyEndpointImpl: ServerEndpoint[Any, Future]                                    =
    verifyEndpoint
      .serverLogic(authOpt => Future(authController.verify(authOpt)))


  override val all: List[PublicEndpoint[?, ?, ?, ?]]      = List(authEndpoint, verifyEndpoint)
  override val allImpl: List[ServerEndpoint[Any, Future]] =
    List(authEndpointImpl, verifyEndpointImpl)
