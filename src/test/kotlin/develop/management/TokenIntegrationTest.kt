package develop.management

import com.google.gson.GsonBuilder
import develop.management.domain.document.Key
import develop.management.domain.document.Room
import develop.management.domain.document.Service
import develop.management.domain.dto.CreateOptions
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.ServiceConfig
import develop.management.domain.dto.TokenConfig
import develop.management.repository.KeyRepository
import develop.management.repository.RoomRepository
import develop.management.repository.ServiceRepository
import develop.management.repository.TokenRepository
import develop.management.util.cipher.Cipher
import develop.management.util.error.ErrorBody
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

/**
 * Token 관련 요청 테스트 클래스
 * Authentication, authorization, validation error는 고려하지 않는다
 * -> Authentication, authorization, vadlidation error는 따로 테스트 객체를 만든다
 */
@SpringBootTest(classes = [DevelopApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TokenIntegrationTest {

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var keyRepository: KeyRepository

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @LocalServerPort
    private var port: Int = 0

    private lateinit var auth: String

    private lateinit var service: Service

    private lateinit var room: Room

    @BeforeEach
    fun init() {
        val key = runBlocking {
            keyRepository.save(Key.createKey())
            val serviceName = "service_integration_test@" + Cipher.generateByteArray(1).decodeToString()
            val key = Cipher.generateKey(128, "HmacSHA256")
            if(serviceRepository.existsByName(serviceName)) throw AssertionError("Service already exists")
            service = serviceRepository.save(Service.create(ServiceConfig(serviceName, key)))

            val roomName = "service_integration_test@" + Cipher.generateByteArray(1).decodeToString()
            val options = CreateOptions()
            room = roomRepository.save(Room.create(RoomConfig(roomName, options)))
            service.addRoom(room.getId())
            service = serviceRepository.save(service)
            return@runBlocking key
        }

        val cnonce = "cnonce"
        val timestamp = Date().toString()
        val message = "$timestamp,$cnonce"
        val signature = Cipher.createHmac(message, key, "HmacSHA256")
        auth = "MAuth realm=http://marte3.dit.upm.es," +
                "mauth_signature_method=HMAC_SHA256," +
                "mauth_serviceid=" + service.getId() + "," +
                "mauth_cnonce=" + cnonce + "," +
                "mauth_timestamp=" + timestamp + "," +
                "mauth_signature=" + signature
    }

    @AfterEach
    fun close() {
        runBlocking {
            serviceRepository.deleteAll()
            roomRepository.deleteAll()
            tokenRepository.deleteAll()
            keyRepository.deleteAll()
        }
    }

    /** 1. Authentication 테스트 **/
    //Authentication unit test


    /** 2. Authorization 테스트 **/
    /**
     * Service에 존재하지 않는 room에 대한 token 생성 요청 테스트
     * @expected: Permission denied (status: 403, code: 1102)
     */

    @Test
    fun createWithNotExistRoomTest() {
        val roomId = "not_exist_room@" + Cipher.generateByteArray(1).decodeToString()
        runBlocking {
            if (roomRepository.existsById(roomId)) throw AssertionError("Room already exists")
            service = serviceRepository.save(service)
        }
        val tokenRequest = TokenConfig("user", "presenter")
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", roomId)
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
        Assertions.assertEquals("Room not found", message)
    }

    /** 3. Validation 테스트 **/

    /**
     * TokenRequest를 전달하지 않았을 때 token 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, code: 1201)
     */
    @Test
    fun createWithNoTokenRequestTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Required arguments must not be null", message)
    }

    /**
     * 올바르지 않은 tokenRequest를 전달했을때 token 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, code: 1201)
     */
    @Test
    fun createWithWrongTokenRequestTest() {
        val tokenRequest = "test"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Required arguments must not be null", message)
    }

    /**
     * TokenRequest에 user가 null인 경우 token 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, code: 1201)
     */
    @Test
    fun createWithNoUserTokenRequestTest() {
        val tokenRequest = net.minidev.json.JSONObject()
        tokenRequest["role"] = "presenter"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Required arguments must not be null", message)
    }

    /**
     * TokenRequest에 user가 empty 경우 token 생성 요청 테스트
     * @expected: Invalid request body: The user must not be empty. (status: 400, code: 1201)
     */
    @Test
    fun createWithEmptyUserTokenRequestTest() {
        val tokenRequest = TokenConfig("", "presenter")
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: The user must not be empty. ", message)
    }

    /**
     * TokenRequest에 role이 null 경우 token 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, code: 1201)
     */
    @Test
    fun createWithNoRoleTokenRequestTest() {
        val tokenRequest = net.minidev.json.JSONObject()
        tokenRequest["user"] = "token_request_validator_test_user"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Required arguments must not be null", message)
    }

    /**
     * TokenRequest에 role이 empty 경우 token 생성 요청 테스트
     * @expected: Invalid request body: The role must not be empty. (status: 400, code: 1201)
     */
    @Test
    fun createWithEmptyRoleTokenRequestTest() {
        val tokenRequest = TokenConfig("user", "")
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: The role must not be empty. ", message)
    }

    /** 4. Service 테스트 **/

    /**
     * 새로운 token 생성 요청 테스트
     * @expected: DB에 token이 생성되고 생성된 token을 string으로 반환 받는다
     */
    @Test
    fun createTest() {
        val tokenRequest = TokenConfig("user", "presenter")
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
        val tokenStr = Base64.getDecoder().decode(result.responseBody!!).decodeToString()
        val tokenId = JSONObject(tokenStr).getString("tokenId")
        val token = runBlocking { tokenRepository.findById(tokenId) }?: throw AssertionError("Token does not exist")
        Assertions.assertEquals(room.getId(), token.getRoomId())
        Assertions.assertEquals(service.getId(), token.getServiceId())
    }

    /**
     * room의 role과 일치하는 role이 없을 때 token 생성 요청 테스트
     * @expected: Role is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun createTokenNotMatchRoleTest() {
        val tokenRequest = TokenConfig("user", "test")
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Role is not valid", message)
    }

    /**
     * TokenRequest를 전달하지 않았을 때 token 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun createTokenWithNoTokenRequestTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Required arguments must not be null", message)
    }

    //Todo: Rpc error도 확인해야 한다

    /**
     * Token 생성을 요청할 때, key가 없는 경우 테스트
     * @expected: Create token fail: Key does not exist (status: 500, error_code: 2001)
     */
    @Test
    fun createTokenWhenKeyNotExistTest() {
        runBlocking {
            keyRepository.deleteById(0)
        }
        val tokenRequest = TokenConfig("user", "presenter")
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms/{roomId}/tokens", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .is5xxServerError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(500, status)
        Assertions.assertEquals(2001, code)
        Assertions.assertEquals("Create token fail: Key does not exist", message)
    }
}

