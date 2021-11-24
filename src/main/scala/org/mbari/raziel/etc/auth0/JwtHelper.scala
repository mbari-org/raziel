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

package org.mbari.raziel.etc.auth0

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.JWT
import java.time.{Duration, Instant}
import java.util.Date
import org.mbari.raziel.AppConfig
import scala.jdk.CollectionConverters.given
import scala.util.Try

class JwtHelper(issuer: String, signingSecret: String, expiration: Duration):

  private val algorithm = Algorithm.HMAC512(signingSecret)

  val verifier = JWT
    .require(algorithm)
    .withIssuer(issuer)
    .build()

  def createJwt(payload: Map[String, Any]): String =
    val now     = Instant.now()
    val expires = now.plus(expiration)
    JWT
      .create()
      .withPayload(payload.asJava)
      .withIssuer(issuer)
      .withIssuedAt(Date.from(now))
      .withExpiresAt(Date.from(expires))
      .sign(algorithm)

  def verifyJwt(token: String): Either[Throwable, DecodedJWT] =
    Try(verifier.verify(token)).toEither

object JwtHelper:

  lazy val default =
    JwtHelper(AppConfig.Jwt.Issuer, AppConfig.Jwt.SigningSecret, AppConfig.Jwt.Expiration)
