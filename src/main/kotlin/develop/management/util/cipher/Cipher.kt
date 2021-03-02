package develop.management.util.cipher

import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 암호화, 복호화 관련된 작업을 처리하는 클래스
 */
class Cipher {

    companion object {

        //32byte secure random key
        //암호화, 복호화 시에 secret key를 만들기 위해 사용한다
        private const val key: String = "3d84a1efc77268c98bc6ca2921eb35a6d82a40a38696d25fbb64ff1733cd2523"

        //16byte secure random initialization vector
        //암호화, 복호화 시에 initialization vector를 만들기 위해 사용한다
        private const val iv: String = "3a613563c433a361a62a9efe4dd8462e"

        /**
         * key를 이용하여 data를 암호화한다
         * String을 utf-8로 인코딩하여 byteArray를 만들고 암호화한 뒤 이를 다시 hex string으로 인코딩하여 반환한다
         * input -> utf-8 -> byteArray -> hex -> output
         * @param data : 암호화할 string 데이터
         * @param key : Secret key를 생성하기 위해 사용하는 기본 key
         * @return : Hex encoded string
         */
        fun encrypt(data: String, key: String = Companion.key): String {
            //Todo: key를 건들지 못하도록 할 수 없나?
            val byteData = data.encodeToByteArray()
            val cipher: Cipher = Cipher.getInstance("AES/CTR/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(decodeHexString(key), "AES"), IvParameterSpec(
                decodeHexString(
                    iv
                )
            ))
            return encodeHexByteArray(cipher.doFinal(byteData))
        }

        /**
         * key를 이용하여 암호화된 data를 복호화한다
         * Hex String을 byte array로 디코딩하고 암호화한 뒤 이를 utf-8로 string으로 디코딩하여 반환한다
         * input -> hex -> byteArray -> utf-89 -> output
         * @param data : 암호화할 string 데이터 (hex encoded string)
         * @param key : Secret key를 생성하기 위해 사용하는 기본 key
         * @return : utf decoded string
         */
        fun decrypt(data: String, key: String = Companion.key): String {
            //Todo: key를 건들지 못하도록 할 수 없나?
            val byteData = decodeHexString(data)
            val cipher: Cipher = Cipher.getInstance("AES/CTR/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(decodeHexString(key), "AES"), IvParameterSpec(
                decodeHexString(
                    iv
                )
            ))
            return cipher.doFinal(byteData).decodeToString()
        }

        /**
         * Data와 key를 조합하여 hash를 만들고 hash를 hex 인코딩후 base64로 인코딩한다
         * @param data : String
         * @param key : String key (여기에서는 base64로 인코딩된 string key를 사용한다)
         * @param algorithm : Hmac 해쉬 생성시 사용하는 알고리즘
         * @return : Base64로 인코딩된 string
         */
        fun createHmac(data: String, key: String, algorithm: String): String {
            val hasher: Mac = Mac.getInstance(algorithm)
            hasher.init(SecretKeySpec(key.encodeToByteArray(), algorithm))
            val hash: ByteArray = hasher.doFinal(data.encodeToByteArray())
            val hex: String = encodeHexByteArray(hash)
            return Base64.getEncoder().encodeToString(hex.encodeToByteArray())
        }

        /**
         * Random한 key를 생성해준다
         * @param size: 생성할 key의 size (Byte)
         * @param algorithm: key를 생성할 때 사용할 algorithm
         * @return : base64 encoded string
         */
        fun generateKey(size: Int, algorithm: String): String {
            val keyGenerator = KeyGenerator.getInstance(algorithm)
            keyGenerator.init(size*8)
            val byteKey = keyGenerator.generateKey().encoded
            return Base64.getEncoder().encodeToString(byteKey)
        }

        /**
         * Secure random한 byteArray를 생성한다
         * @param size : 생성할 byteArray의 size (bytes)
         * @return : ByteArray
         */
        fun generateByteArray(size: Int): ByteArray {
            val temp = ByteArray(size)
            SecureRandom().nextBytes(temp)
            return temp
        }

        /**
         * byte를 hex string으로 바꿔주는 함수
         */
        fun byteToHex(num: Byte): String {
            val hexDigits = CharArray(2)
            hexDigits[0] = Character.forDigit(num.toInt() shr 4 and 0xF, 16)
            hexDigits[1] = Character.forDigit(num.toInt() and 0xF, 16)
            return String(hexDigits)
        }

        /**
         * ByteArray를 Hex String으로 인코딩하는 함수
         */
        fun encodeHexByteArray(byteArray: ByteArray): String {
            val hexStringBuffer = StringBuffer()
            for (i in byteArray.indices) {
                hexStringBuffer.append(byteToHex(byteArray[i]))
            }
            return hexStringBuffer.toString()
        }

        /**
         * Hex String을 byte로 바꿔주는 함수
         */
        fun hexToByte(hexString: String): Byte {
            val firstDigit = toDigit(hexString[0])
            val secondDigit = toDigit(hexString[1])
            return ((firstDigit shl 4) + secondDigit).toByte()
        }

        /**
         * Hex character를 digital 값으로 바꿔주는 함수
         */
        private fun toDigit(hexChar: Char): Int {
            val digit = Character.digit(hexChar, 16)
            require(digit != -1) { "Invalid Hexadecimal Character: $hexChar" }
            return digit
        }

        /**
         * Hex string을 byteArray로 디코딩하는 함수
         */
        fun decodeHexString(hexString: String): ByteArray {
            require(hexString.length % 2 != 1) { "Invalid hexadecimal String supplied." }
            val bytes = ByteArray(hexString.length / 2)
            var i = 0
            while (i < hexString.length) {
                bytes[i / 2] = hexToByte(hexString.substring(i, i + 2))
                i += 2
            }
            return bytes
        }
    }
}