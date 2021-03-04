package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.ManagementInitializer
import develop.management.auth.ServiceAuthenticator.AuthData
import develop.management.domain.document.Room
import develop.management.domain.document.Service
import develop.management.domain.dto.CreateOptions
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.ServiceConfig
import develop.management.repository.KeyRepository
import develop.management.repository.RoomRepository
import develop.management.repository.ServiceRepository
import develop.management.util.cipher.Cipher
import develop.management.util.error.ErrorBody
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

/**
 * Room Validation 테스트 클래스
 * Server 전체를 bind하지 않고 router function만 bind한다
 */
@SpringBootTest
internal class RoomValidatorTest {

    @Autowired
    private lateinit var initializer: ManagementInitializer

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var keyRepository: KeyRepository

    @Autowired
    private lateinit var roomValidator: RoomValidator

    private lateinit var serviceAuth: AuthData

    private lateinit var service: Service

    private lateinit var superService: Service

    private lateinit var room: Room

    private lateinit var notInServiceRoom: Room

    @BeforeEach
    fun init() {
        //SuperService 생성
        initializer.init()
        val superServiceId = initializer.getSuperServiceId()
        superService = runBlocking { serviceRepository.findById(superServiceId) }?: throw AssertionError("SuperService does not exist")

        //authData 생성
        val serviceName = "auth_test_service" + Cipher.generateByteArray(1).decodeToString()
        runBlocking {
            if(serviceRepository.existsByName(serviceName)) throw AssertionError("Service already exists")

            val serviceKey = Cipher.generateKey(128, "HmacSHA256")
            val serviceData = Service.create(ServiceConfig(serviceName, serviceKey))
            val roomConfig = RoomConfig("name", CreateOptions())

            service = serviceRepository.save(serviceData)
            room = roomRepository.save(Room.create(roomConfig))
            service.addRoom(room.getId())
            service = serviceRepository.save(service)
            notInServiceRoom = roomRepository.save(Room.create(roomConfig))
        }
    }

    @AfterEach
    fun close() {
        runBlocking {
            serviceRepository.deleteAll()
            roomRepository.deleteAll()
            keyRepository.deleteAll()
        }
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
                .uri("/rooms/{roomId}", room.getId())
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
                .uri("/rooms/{roomId}", notInServiceRoom.getId())
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

