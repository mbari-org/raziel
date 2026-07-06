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

import org.mbari.raziel.util.crypto.AES

final case class EncodedAuth(encodedUsername: String, encodedPassword: String):

    def asBasicAuth(aesKey: String): Either[ErrorMsg, BasicAuth] =
        val aes = new AES(aesKey)
        for
            username <- aes.decrypt(encodedUsername).left.map(e => Unauthorized("Invalid encoded credentials"))
            password <- aes.decrypt(encodedPassword).left.map(e => Unauthorized("Invalid encoded credentials"))
        yield BasicAuth(username, password)

object EncodedAuth:

    def fromBasicAuth(auth: BasicAuth, aesKey: String): Either[ErrorMsg, EncodedAuth] =
        val aes = new AES(aesKey)
        for
            encodedUsername <- aes.encrypt(auth.username).left.map(e => ServerError("Unable to encode credentials"))
            encodedPassword <- aes.encrypt(auth.password).left.map(e => ServerError("Unable to encode credentials"))
        yield EncodedAuth(encodedUsername, encodedPassword)
