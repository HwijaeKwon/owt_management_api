package develop.management.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import develop.management.domain.document.Key
import develop.management.domain.document.Room
import develop.management.domain.dto.CreateOptions
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.TokenConfig
import develop.management.repository.mongo.*
import develop.management.rpc.RpcService
import develop.management.rpc.RpcServiceResult
import develop.management.util.cipher.Cipher
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.util.*

/**
 * Token Service 테스트 클래스
 * Authenticator, authorization, validation error는 고려하지 않는다
 * -> Authenticator, authorization, vadlidation error는 따로 테스트 클래스를 만든다
 */

@ActiveProfiles("test")
@SpringBootTest(classes = [MongoTokenRepository::class, MongoRoomRepository::class, MongoKeyRepository::class, TokenService::class, RpcService::class])
@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
@Import(TestReactiveMongoConfig::class)
internal class TokenServiceTest {

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var tokenRepository: MongoTokenRepository

    @Autowired
    private lateinit var roomRepository: MongoRoomRepository

    @Autowired
    private lateinit var keyRepository: MongoKeyRepository

    @MockBean
    private lateinit var rpcService: RpcService

    private lateinit var room: Room

    private val serviceId = "token_service_test_service@" + Cipher.generateByteArray(1).decodeToString()

    @BeforeEach
    fun init() {
        val roomName = "token_service_test_room@" + Cipher.generateByteArray(1).decodeToString()
        val options = CreateOptions()
        runBlocking {
            room = roomRepository.save(Room.create(RoomConfig(roomName, options)))
            if(!keyRepository.existsById(0)) keyRepository.save(Key.createKey())
        }
    }

    @AfterEach
    fun close() {
        runBlocking {
            roomRepository.deleteAll()
            tokenRepository.deleteAll()
            keyRepository.deleteAll()
        }
    }

    /**
     * 새로운 token 생성 요청 테스트
     * @expected: DB에 token이 생성되고, 생성된 token를 반환받는다
     */
    @Test
    fun createTest() = runBlocking {
        val tokenRequest = TokenConfig("user", "presenter")

        val jsonResult = JSONObject()
        jsonResult.put("ip", "http://test")
        jsonResult.put("hostname", "test")
        jsonResult.put("port", 12345)
        jsonResult.put("ssl", true)
        jsonResult.put("state", 2)
        jsonResult.put("max_load", 3)
        jsonResult.put("capacity", 3)

        whenever(rpcService.schedulePortal(any(), eq(tokenRequest.preference))).thenReturn(RpcServiceResult("success", jsonResult.toString()))

        val result = runBlocking { tokenService.create(serviceId, room.getId(), tokenRequest.user, tokenRequest.role, tokenRequest.preference) }
        Assertions.assertNotNull(result)
        println(result)
        val tokenStr = Base64.getDecoder().decode(result).decodeToString()
        val tokenId = JSONObject(tokenStr).getString("tokenId")
        val token = tokenRepository.findById(tokenId)?: throw AssertionError("Token does not exist")
        Assertions.assertEquals(room.getId(), token.getRoomId())
        Assertions.assertEquals(serviceId, token.getServiceId())
    }

    /**
     * TokenRequest의 role과 room의 role이 다른 경우 테스트
     * @expected: Exception("Role is not valid")
     */
    @Test
    fun notMatchRoleTest() {
        val tokenRequest = TokenConfig("user", "test")
        val exception = Assertions.assertThrows(Exception::class.java) { runBlocking { tokenService.create(serviceId, room.getId(), tokenRequest.user, tokenRequest.role, tokenRequest.preference) } }
        Assertions.assertEquals("Role is not valid", exception.message)
    }

    /**
     * Key가 없는 경우 token 생성 요청 테스트
     * @expected: IllegalStateException("Key does not exist")
     */
    @Test
    fun notExistKeyTest() {
        runBlocking {
            keyRepository.deleteById(0)
            val tokenRequest = TokenConfig("user", "presenter")

            val jsonResult = JSONObject()
            jsonResult.put("ip", "http://test")
            jsonResult.put("hostname", "test")
            jsonResult.put("port", 12345)
            jsonResult.put("ssl", true)
            jsonResult.put("state", 2)
            jsonResult.put("max_load", 3)
            jsonResult.put("capacity", 3)
            whenever(rpcService.schedulePortal(any(), eq(tokenRequest.preference))).thenReturn(RpcServiceResult("success", jsonResult.toString()))

            val exception = Assertions.assertThrows(IllegalStateException::class.java) { runBlocking { tokenService.create(serviceId, room.getId(), tokenRequest.user, tokenRequest.role, tokenRequest.preference) } }
            Assertions.assertEquals("Key does not exist", exception.message)
        }
    }
}