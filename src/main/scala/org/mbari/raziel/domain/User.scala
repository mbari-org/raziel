/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.domain

import org.jasypt.util.password.BasicPasswordEncryptor

case class User(
    username: String,
    password: String,
    role: Option[String],
    affiliation: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    email: Option[String]
):

  def authenticate(unencryptedPassword: String): Boolean =
    BasicPasswordEncryptor().checkPassword(unencryptedPassword, password)
