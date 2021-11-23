/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.domain

case class Authorization(tokenType: String, accessToken: String)

object Authorization:
  def parse(authorization: String): Option[Authorization] =
    val parts = authorization.split("\\s+")
    if (parts.length == 2)
      Some(Authorization(parts(0), parts(1)))
    else
      None

  def Invalid: Authorization = Authorization("", "")
