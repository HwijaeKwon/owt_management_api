package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.ManagementInitializer
import develop.management.auth.ServiceAuthenticator.AuthData
import develop.management.domain.document.Room
import develop.management.domain.document.Service
import develop.management.domain.dto.CreateOptions
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.ServiceConfig
import develop.management.repository.mongo.MongoRoomRepository
import develop.management.repository.mongo.TestReactiveMongoConfig
import develop.management.util.cipher.Cipher
import develop.management.util.error.ErrorBody
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

/**
 * Room Validation 테스트 클래스
 * Server 전체를 bind하지 않고 router function만 bind한다
 */
@SpringBootTest(classes = [TestReactiveMongoConfig::class, MongoRoomRepository::class, RoomValidator::class])
@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
internal class RoomValidatorTest {

    @MockBean
    private lateinit var roomRepository: MongoRoomRepository

    @Autowired
    private lateinit var roomValidator: RoomValidator

    private lateinit var serviceAuth: AuthData

    private lateinit var service: Service

    private lateinit var room: Room

    private lateinit var notInServiceRoom: Room

    @BeforeEach
    fun init() = runBlocking {
        service = Service.create(ServiceConfig("serviceName", "serviceKey"))
        val roomConfig = RoomConfig("name", CreateOptions())
        room = Room.create(roomConfig)
        service.addRoom("roomId")
        notInServiceRoom = Room.create(roomConfig)

        Mockito.`when`(roomRepository.findById("roomId")).thenReturn(room)
        Mockito.`when`(roomRepository.findById("notInServiceRoomId")).thenReturn(notInServiceRoom)
        return@runBlocking
    }

    fun roomRouter(): RouterFunction<ServerResponse> = coRouter {
        POST("/rooms") {
            ServerResponse.ok().bodyValueAndAwait("Server authorization success")
        }
        GET("/rooms/{roomId}") {
            ServerResponse.ok().bodyValueAndAwait("Server authorization success")
        }
        filter { request, next ->
            request.attributes()["authData"] = serviceAuth
            next(request)
        }
        filter(roomValidator::validate)
    }

    /**
     * room 유효성 테스트
     * @expected: Authorization success (status: 200)
     */
    @Test
    fun roomAuthTest() {
        serviceAuth = AuthData(service, "auth_test_user", "auth_test_role")
        val result = WebTestClient
                .bindToRouterFunction(roomRouter())
                .build()
                .get()
                .uri("/rooms/{roomId}", "roomId")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * 서비스에 존재하지 않는 room 유효성 테스트
     * @expected: Not found error (status: 404, error_code: 1001)
     */
    @Test
    fun notInServiceRoomAuthTest() {
        serviceAuth = AuthData(service, "auth_test_user", "auth_test_role")

        val result = WebTestClient
                .bindToRouterFunction(roomRouter())
                .build()
                .get()
                .uri("/rooms/{roomId}", "notInServiceRoomId")
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
    }

    /**
     * DB에 존재하지 않는 room 유효성 테스트
     * @expected: Not found error (status: 404, error_code: 1003)
     */
    @Test
    fun notExistRoomAuthTest() {
        serviceAuth = AuthData(service, "auth_test_user", "auth_test_role")

        //임의의 roomId
        val roomId = Cipher.generateByteArray(1).decodeToString()

        val result = WebTestClient
                .bindToRouterFunction(roomRouter())
                .build()
                .get()
                .uri("/rooms/{roomId}", roomId)
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
    }
}

