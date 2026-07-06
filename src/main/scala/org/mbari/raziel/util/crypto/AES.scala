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

package org.mbari.raziel.util.crypto

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays
import java.util.Base64
import scala.util.Try
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AES(key: String):

    private val secretKey: Either[Throwable, SecretKeySpec] = 
        try
            val keyBytes = key.getBytes("UTF-8")
            val sha      = java.security.MessageDigest.getInstance("SHA-1")
            val hashed   = sha.digest(keyBytes)
            val keyBytes16 = hashed.take(16) // Use only the first 16 bytes for AES-128
            Right(new SecretKeySpec(keyBytes16, "AES"))
        catch
            case e: UnsupportedEncodingException => Left(e)
            case e: NoSuchAlgorithmException     => Left(e)

    def encrypt(strToEncrypt: String): Either[Throwable, String] =
        secretKey.flatMap { secretKeySpec => 
            try
                val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
                val encryptedBytes = cipher.doFinal(strToEncrypt.getBytes("UTF-8"))
                Right(Base64.getEncoder.encodeToString(encryptedBytes))
            catch
                case e: Exception => Left(e)
        }

    def decrypt(strToDecrypt: String): Either[Throwable, String] =
        secretKey.flatMap { secretKeySpec =>
            try
                val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
                val decryptedBytes = cipher.doFinal(Base64.getDecoder.decode(strToDecrypt))
                Right(new String(decryptedBytes, "UTF-8"))
            catch
                case e: Exception => Left(e)
        }
  
