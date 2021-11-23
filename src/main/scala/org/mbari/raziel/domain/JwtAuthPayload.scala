/*
 * Copyright (c) Monterey Bay Aquarium Research Institute 2021
 *
 * raziel code is non-public software. Unauthorized copying of this file,
 * via any medium is strictly prohibited. Proprietary and confidential. 
 */

package org.mbari.raziel.domain

case class JwtAuthPayload(username: String, email: String, affiliation: String):
  def asMap(): Map[String, Any] = (productElementNames zip productIterator).toMap

object JwtAuthPayload:

  def fromUser(user: User): JwtAuthPayload =
    JwtAuthPayload(user.username, user.email.getOrElse(""), user.affiliation.getOrElse(""))
