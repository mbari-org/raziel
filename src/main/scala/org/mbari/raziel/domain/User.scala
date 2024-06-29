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

import org.jasypt.util.password.BasicPasswordEncryptor

/**
 * Maps the users from the vars-users-server to a case class
 */
case class User(
    username: String,
    password: String,
    role: Option[String],
    affiliation: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    email: Option[String]
):

    /**
     * @param unencryptedPassword
     *   A users password, received via basic authentication It's checked to see if it's valid for the encrypted data
     *   from the database
     */
    def authenticate(unencryptedPassword: String): Boolean =
        BasicPasswordEncryptor().checkPassword(unencryptedPassword, password)
