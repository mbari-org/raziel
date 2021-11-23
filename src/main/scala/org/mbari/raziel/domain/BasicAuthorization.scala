/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.domain

import java.util.Base64

case class BasicAuthorization(username: String, password: String)

object BasicAuthorization:
  def decode(authorization: String): Option[BasicAuthorization] =
    val bytes   = Base64.getDecoder.decode(authorization)
    val decoded = new String(bytes)
    val parts   = decoded.split(":")
    if (parts.length == 2)
      Some(BasicAuthorization(parts(0), parts(1)))
    else
      None
