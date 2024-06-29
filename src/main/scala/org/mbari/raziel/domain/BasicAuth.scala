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

package org.mbari.raziel.domain

import java.util.Base64
import java.nio.charset.StandardCharsets

/**
 * @param username
 *   The username from a Basic Auth header
 * @param password
 *   The password from a Basic Auth header
 * @author
 *   Brian Schlining
 */
case class BasicAuth(username: String, password: String) extends Auth(BasicAuth.TokenType)

object BasicAuth:

    val TokenType = "Basic"

    /**
     * Parse a Basic Authorization header: "Basic <base64-encoded-username:password>"
     *
     * @param authorization
     *   The value portion of the Authorization header.
     * @return
     *   The parse BasicAuth. None if it's no parsable
     */
    def parse(authorization: String): Option[BasicAuth] =
        val parts = authorization.split("\\s+")
        if parts.length == 2 && parts(0).toLowerCase == TokenType.toLowerCase then
            val bytes        = Base64.getDecoder.decode(parts(1))
            val decoded      = new String(bytes, StandardCharsets.UTF_8)
            val decodedParts = decoded.split(":")
            if decodedParts.length == 2 then Some(BasicAuth(decodedParts(0), decodedParts(1)))
            else None
        else None
