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

import org.mbari.raziel.domain.BearerAuth
import org.mbari.raziel.domain.ErrorMsg
import org.mbari.raziel.domain.Unauthorized
import org.mbari.raziel.AppConfig
import org.mbari.raziel.etc.auth0.JwtHelper
import org.mbari.raziel.domain.BasicAuth
import zio.IO
import org.mbari.raziel.services.VarsUserServer
import scala.util.Try
import scala.util.Success
import org.mbari.raziel.domain.ServerError
import scala.util.Failure
import zio.Task
import scala.jdk.CollectionConverters.*
import org.mbari.raziel.domain.JwtAuthPayload

class AuthController(varsUserServer: VarsUserServer):

  private val jwtHelper = JwtHelper.default
  private val runtime   = zio.Runtime.default

  def authenticate(xApiKey: Option[String], auth: Option[BasicAuth]): Either[ErrorMsg, BearerAuth] = 
    xApiKey match
      case Some(key) =>
        if (key == AppConfig.MasterKey)
          val token = jwtHelper.createJwt(Map("username" -> "master"))
          Right(BearerAuth(token))
        else Left(Unauthorized("Invalid credentials"))
      case None      =>

        val app = for
          a       <- IO.fromEither(auth.toRight(Unauthorized("Missing or invalid basic credentials")))
          // _  <- Task.succeed(log.info(s"auth: $a"))
          u       <- varsUserServer.Users.findByName(a.username)
          // _  <- Task.succeed(log.info(s"user: $u"))
          ok      <- Task.succeed(u.map(v => v.authenticate(a.password)).getOrElse(false))
          payload <- Task.succeed(
                       if (ok)
                         Some(JwtAuthPayload.fromUser(u.get))
                       else
                         None
                     )
        yield payload

        Try(runtime.unsafeRun(app)) match
          case Success(payload) =>
            payload match
              case Some(p) =>
                val token = jwtHelper.createJwt(p.asMap())
                Right(BearerAuth(token))
              case None    =>
                Left(Unauthorized("Invalid credentials"))
          case Failure(e)       =>
            Left(ServerError(e.getMessage))

  def authenticateRaw(
      xApiKey: Option[String] = None,
      authorization: Option[String] = None
  ): Either[ErrorMsg, BearerAuth] =
    authenticate(xApiKey, authorization.flatMap(BasicAuth.parse))

  def verify(tokenOpt: Option[String]): Either[ErrorMsg, Map[String, String]] = 
    val either = for
      token          <- tokenOpt.toRight(Unauthorized("Missing or invalid token"))
      decodedJwt <- jwtHelper.verifyJwt(token)
    yield decodedJwt

    either match
      case Right(jwt) =>
        val claims = jwt
          .getClaims
          .asScala
          .toMap
          .filter((key, claim) => claim.asString != null && claim.asString.nonEmpty)
          .map((key, claim) => (key, claim.asString()))

        Right(claims)

      case Left(e) =>
        Left(Unauthorized(s"Invalid credentials: ${e.getClass}"))


  def verifyRaw(authorization: Option[String]): Either[ErrorMsg, Map[String, String]] =
    
    val tokenOpt = authorization
      .flatMap(BearerAuth.parse)
      .map(_.accessToken)

    verify(tokenOpt)