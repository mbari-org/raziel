/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.api

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import java.util.concurrent.Executor
import org.mbari.raziel.AppConfig
import org.mbari.raziel.domain.{Authorization, BasicAuthorization, ErrorMsg, JwtAuthPayload}
import org.mbari.raziel.etc.circe.CirceCodecs.{given, _}
import org.mbari.raziel.services.VarsUserServer
import org.scalatra.{FutureSupport, InternalServerError, ScalatraServlet, Unauthorized}
import org.slf4j.LoggerFactory
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import zio.*
import scala.jdk.CollectionConverters.given
import java.util.Date
import java.time.Instant
import org.mbari.raziel.etc.auth0.JwtHelper

class AuthApi(varsUserServer: VarsUserServer) extends ScalatraServlet:

  private val jwtHelper = JwtHelper.default
  private val runtime   = zio.Runtime.default
  private val log       = LoggerFactory.getLogger(getClass)

  after() {
    contentType = "application/json"
  }

  /*
    --- REQUEST:
    POST /auth
    Authorization: Basic base64(username:password)

    --- RESPONSE 200:
    HTTP/1.1 200 OK
    Connection: close
    Date: Mon, 22 Nov 2021 22:53:47 GMT
    Content-Type: application/json;charset=utf-8
    Content-Length: 338

    {
      "tokenType": "Bearer",
      "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhZmZpbGlhdGlvbiI6Ik1CQVJJIiwiaXNzIjoiaHR0cDovL3d3dy5tYmFyaS5vcmciLCJleHAiOjE2NTMxNzM2MjcsImlhdCI6MTYzNzYyMTYyNywiZW1haWwiOiJicmlhbkBtYmFyaS5vcmciLCJ1c2VybmFtZSI6ImJyaWFuIn0.FuGr9NoQjbrHKPUPvRmscmGjKWYkTfsqNcgnAbrZDvnGpq0gv31kz5qFAY6Ve5KQUouAttlh0aU5ny-pqxOrCg"
    }

    --- RESPONSE 401:
    HTTP/1.1 401 Unauthorized
    Connection: close
    Date: Mon, 22 Nov 2021 22:56:29 GMT
    Content-Type: application/json;charset=utf-8
    Content-Length: 52

    {
      "message": "Invalid credentials",
      "responseCode": 401
    }

   */
  post("/") {

    val auth = Option(request.getHeader("Authorization"))
      .flatMap(a => BasicAuthorization.decode(a))
      .toRight(new IllegalArgumentException("Authorization header required"))

    val app = for
      a       <- IO.fromEither(auth)
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
            val token   = jwtHelper.createJwt(p.asMap())
            Authorization("Bearer", token).asJson.noSpaces
          case None    =>
            halt(Unauthorized(ErrorMsg("Invalid credentials", 401).asJson.print))
      case Failure(e)       =>
        halt(InternalServerError(ErrorMsg(e.getMessage, 401).asJson.print))

  }

  /*
    --- REQUEST:
    POST /auth/verify
    Authorization: Bearer <JWT>

    --- RESPONSE 200:
    HTTP/1.1 200 OK
    Connection: close
    Date: Mon, 22 Nov 2021 22:55:27 GMT
    Content-Type: application/json;charset=utf-8
    Content-Length: 97

    {
      "username": "brian",
      "iss": "http://www.mbari.org",
      "email": "brian@mbari.org",
      "affiliation": "MBARI"
    }

    --- RESPONSE 401:
    HTTP/1.1 401 Unauthorized
    Connection: close
    Date: Mon, 22 Nov 2021 22:55:50 GMT
    Content-Type: application/json;charset=utf-8
    Content-Length: 52

    {
      "message": "Invalid credentials",
      "responseCode": 401
    }

   */
  post("/verify") {

    val auth = Option(request.getHeader("Authorization"))
      .flatMap(a => Authorization.parse(a))
      .toRight(throw new IllegalArgumentException("Authorization header required"))

    val either = for
      a          <- auth
      decodedJwt <- jwtHelper.verifyJwt(a.accessToken)
    yield decodedJwt

    either match
      case Right(jwt) =>
        jwt
          .getClaims
          .asScala
          .toMap
          .filter((key, claim) => claim.asString != null && claim.asString.nonEmpty)
          .map((key, claim) => (key, claim.asString()))
          .asJson
          .print

      case Left(e) =>
        halt(Unauthorized(ErrorMsg(s"Invalid credentials: ${e.getClass}", 401).asJson.print))

  }
