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

import io.circe.generic.auto.*
import org.mbari.raziel.domain.{BearerAuth, ErrorMsg}
import org.mbari.raziel.etc.circe.CirceCodecs.given
import scala.concurrent.{ExecutionContext, Future}
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import org.mbari.raziel.etc.jdk.Logging.given
import sttp.tapir.model.UsernamePassword
import sttp.model.headers.WWWAuthenticateChallenge
import org.mbari.raziel.domain.BasicAuth
import org.mbari.raziel.domain.Unauthorized
import org.mbari.raziel.AppConfig

private case class EncodeRequest(url: String)

class AuthEndpoints(authController: AuthController, context: String = "config", aesKey: String = AppConfig.AesKey)(using
    ec: ExecutionContext
) extends Endpoints:

    val authEndpoint: Endpoint[Option[UsernamePassword], Option[String], ErrorMsg, BearerAuth, Any] =
        baseEndpoint
            .post
            .in(context / "auth")
            .securityIn(auth.basic[Option[UsernamePassword]](WWWAuthenticateChallenge.basic))
            .in(header[Option[String]]("X-Api-Key"))
            .out(jsonBody[BearerAuth])
            .name("authenticate")
            .description("Exchange an API key or user credentials for a JWT")
            .tag("auth")
    val authEndpointImpl: ServerEndpoint[Any, Future]                                               =
        authEndpoint
            .serverSecurityLogic(userPwdOpt =>
                Future.successful(Right(userPwdOpt.map(up => BasicAuth(up.username, up.password.get))))
            )
            .serverLogic(basicAuthOpt =>
                xApiKeyOpt =>
//          log.atInfo.log(() => s"Found headers $xApiKeyOpt, $basicAuthOpt")
                    Future(authController.authenticate(xApiKeyOpt, basicAuthOpt))
            )

    val verifyEndpoint: Endpoint[Option[String], Unit, ErrorMsg, Map[String, String], Any] =
        baseEndpoint
            .post
            .in(context / "auth" / "verify")
            .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
            .out(jsonBody[Map[String, String]])
            .name("verifyAuthentication")
            .description("Verify the user's JWT token")
            .tag("auth")
    val verifyEndpointImpl: ServerEndpoint[Any, Future]                                    =
        verifyEndpoint
            .serverSecurityLogic(tokenOpt => Future.successful(Right(tokenOpt)))
            .serverLogic(tokenOpt => _ => Future(authController.verify(tokenOpt)))


    val encodeEndpoint: Endpoint[Option[UsernamePassword], EncodeRequest, ErrorMsg, String, Any] =
        baseEndpoint
            .post
            .in(context / "auth" / "encode")
            .securityIn(auth.basic[Option[UsernamePassword]](WWWAuthenticateChallenge.basic))
            .in(jsonBody[EncodeRequest])
            .out(stringBody)
            .name("encodeBasicAuth")
            .description("Authenticate with BasicAuth and return a login file (url, AES-encoded username, AES-encoded password)")
            .tag("auth")

    val encodeEndpointImpl: ServerEndpoint[Any, Future] =
        encodeEndpoint
            .serverSecurityLogic(userPwdOpt =>
                Future.successful(Right(userPwdOpt.map(up => BasicAuth(up.username, up.password.get))))
            )
            .serverLogic(basicAuthOpt =>
                req =>
                    Future.successful(
                        basicAuthOpt
                            .toRight(Unauthorized("Missing or invalid basic credentials"))
                            .flatMap(auth => authController.encodeToString(auth, req.url))
                    )
            )


    val loginEndpoint: Endpoint[Unit, String, ErrorMsg, BearerAuth, Any] =
        baseEndpoint
            .post
            .in(context / "auth" / "login")
            .in(stringBody)
            .out(jsonBody[BearerAuth])
            .name("authenticateEncoded")
            .description("Authenticate using AES-encoded credentials (3-line text body: url, encoded username, encoded password)")
            .tag("auth")

    val loginEndpointImpl: ServerEndpoint[Any, Future] =
        loginEndpoint
            .serverLogic(body => Future(authController.authenticateEncoded(body)))

    override val all: List[Endpoint[?, ?, ?, ?, ?]]         = List(authEndpoint, verifyEndpoint, encodeEndpoint, loginEndpoint)
    override val allImpl: List[ServerEndpoint[Any, Future]] =
        List(authEndpointImpl, verifyEndpointImpl, encodeEndpointImpl, loginEndpointImpl)
