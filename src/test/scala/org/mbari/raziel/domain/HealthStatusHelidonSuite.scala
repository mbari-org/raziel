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

import cats.syntax.profunctor

class HealthStatusHelidonSuite extends munit.FunSuite:

  test("parseString") {
    val url = getClass.getResource("/charybdis_health.json")
    val s   = scala.io.Source.fromURL(url).getLines.mkString("\n")
    HealthStatusHelidon.parseString(s) match
      case Some(h) =>
        assertEquals(h.freeMemory, 64136504L)
        assertEquals(h.maxMemory, 989855744L)
        assertEquals(h.totalMemory, 112197632L)
      case None    => fail("Expected Some(HealthStatus), got None")

  }
