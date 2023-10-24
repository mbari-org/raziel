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

package org.mbari.raziel

import org.mbari.raziel.util.HexUtil
import java.util.HexFormat
import org.junit.Assert.*

class HexUtilSuite extends munit.FunSuite:

  test("HexUtil and HexFormat match"):
    val s = "how much Woood would a woodchuck chuck if a woodchuck could chuck wood?"
    val a = HexUtil.toHex(s.getBytes)
    val b = HexFormat.of().formatHex(s.getBytes)
    assertEquals(a, b)
    
  
