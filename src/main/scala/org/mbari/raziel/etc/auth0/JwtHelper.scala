/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.etc.auth0

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Duration
import scala.jdk.CollectionConverters.given
import java.time.Instant
import java.util.Date
import org.mbari.raziel.AppConfig
import com.auth0.jwt.interfaces.DecodedJWT
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
