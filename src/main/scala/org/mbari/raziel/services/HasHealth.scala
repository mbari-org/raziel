package org.mbari.raziel.services

import org.mbari.raziel.domain.HealthStatus
import zio.Task


trait HasHealth:

  def health(): Task[Option[HealthStatus]]
