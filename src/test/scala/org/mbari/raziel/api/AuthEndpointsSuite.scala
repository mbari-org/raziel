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

package org.mbari.raziel.api

import munit.FunSuite
import org.mbari.raziel.domain.{BasicAuth, BearerAuth, ErrorMsg, Unauthorized}
import org.mbari.raziel.util.crypto.AES
import sttp.client3.{basicRequest, UriContext}
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.*

class AuthEndpointsSuite extends FunSuite:

    given ec: ExecutionContext = ExecutionContext.global

    val testAesKey   = "testkey"
    val testUsername = "testuser"
    val testPassword = "testpass"
    val testUrl      = "http://example.com/config"

    class FakeAuthController extends AuthController(null, testAesKey):
        override def authenticate(xApiKey: Option[String], auth: Option[BasicAuth]): Either[ErrorMsg, BearerAuth] =
            auth match
                case Some(BasicAuth(`testUsername`, `testPassword`)) => Right(BearerAuth("fake.jwt.token"))
                case Some(_)                                         => Left(Unauthorized("Invalid credentials"))
                case None                                            => Left(Unauthorized("Missing credentials"))

    val controller = FakeAuthController()
    val endpoints  = AuthEndpoints(controller, "config", testAesKey)
    val aes        = AES(testAesKey)

    def makeBackend(serverEndpoints: ServerEndpoint[Any, Future]*) =
        TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointsRunLogic(serverEndpoints.toList)
            .backend()

    def await[T](f: Future[T]): T = Await.result(f, 5.seconds)

    // --- encodeEndpoint tests ---

    test("encode returns 3-line file for valid credentials"):
        val backend  = makeBackend(endpoints.encodeEndpointImpl)
        val response = await(
            basicRequest
                .post(uri"http://localhost/config/auth/encode")
                .auth.basic(testUsername, testPassword)
                .body(s"""{"url":"$testUrl"}""")
                .contentType("application/json")
                .send(backend)
        )
        assertEquals(response.code, StatusCode.Ok)
        val lines = response.body.getOrElse(fail("no response body")).split("\n")
        assertEquals(lines.length, 3)
        assertEquals(lines(0), testUrl)
        assertEquals(aes.decrypt(lines(1)), Right(testUsername))
        assertEquals(aes.decrypt(lines(2)), Right(testPassword))

    test("encode returns 401 for wrong password"):
        val backend  = makeBackend(endpoints.encodeEndpointImpl)
        val response = await(
            basicRequest
                .post(uri"http://localhost/config/auth/encode")
                .auth.basic(testUsername, "wrongpassword")
                .body(s"""{"url":"$testUrl"}""")
                .contentType("application/json")
                .send(backend)
        )
        assertEquals(response.code, StatusCode.Unauthorized)

    test("encode returns 401 when Basic Auth is missing"):
        val backend  = makeBackend(endpoints.encodeEndpointImpl)
        val response = await(
            basicRequest
                .post(uri"http://localhost/config/auth/encode")
                .body(s"""{"url":"$testUrl"}""")
                .contentType("application/json")
                .send(backend)
        )
        assertEquals(response.code, StatusCode.Unauthorized)

    // --- loginEndpoint tests ---

    test("login returns BearerAuth for valid encoded credentials"):
        val encodedUser = aes.encrypt(testUsername).getOrElse(fail("encrypt failed"))
        val encodedPass = aes.encrypt(testPassword).getOrElse(fail("encrypt failed"))
        val body        = s"$testUrl\n$encodedUser\n$encodedPass"
        val backend     = makeBackend(endpoints.loginEndpointImpl)
        val response    = await(
            basicRequest
                .post(uri"http://localhost/config/auth/login")
                .body(body)
                .send(backend)
        )
        assertEquals(response.code, StatusCode.Ok)
        assert(response.body.getOrElse("").contains("fake.jwt.token"))

    test("login returns 401 when decoded credentials fail authentication"):
        val encodedUser = aes.encrypt("baduser").getOrElse(fail("encrypt failed"))
        val encodedPass = aes.encrypt("badpass").getOrElse(fail("encrypt failed"))
        val body        = s"$testUrl\n$encodedUser\n$encodedPass"
        val backend     = makeBackend(endpoints.loginEndpointImpl)
        val response    = await(
            basicRequest
                .post(uri"http://localhost/config/auth/login")
                .body(body)
                .send(backend)
        )
        assertEquals(response.code, StatusCode.Unauthorized)

    test("login returns 401 for body with fewer than 3 lines"):
        val backend  = makeBackend(endpoints.loginEndpointImpl)
        val response = await(
            basicRequest
                .post(uri"http://localhost/config/auth/login")
                .body("only one line")
                .send(backend)
        )
        assertEquals(response.code, StatusCode.Unauthorized)

    // --- round-trip test ---

    test("encode then login round trip succeeds"):
        val encBackend  = makeBackend(endpoints.encodeEndpointImpl)
        val encResponse = await(
            basicRequest
                .post(uri"http://localhost/config/auth/encode")
                .auth.basic(testUsername, testPassword)
                .body(s"""{"url":"$testUrl"}""")
                .contentType("application/json")
                .send(encBackend)
        )
        assertEquals(encResponse.code, StatusCode.Ok)
        val encodedFile = encResponse.body.getOrElse(fail("no encode response body"))

        val loginBackend  = makeBackend(endpoints.loginEndpointImpl)
        val loginResponse = await(
            basicRequest
                .post(uri"http://localhost/config/auth/login")
                .body(encodedFile)
                .send(loginBackend)
        )
        assertEquals(loginResponse.code, StatusCode.Ok)
        assert(loginResponse.body.getOrElse("").contains("fake.jwt.token"))
