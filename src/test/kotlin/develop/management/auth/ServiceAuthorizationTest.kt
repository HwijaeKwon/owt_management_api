package develop.management.auth

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
import develop.management.repository.mongo.TestReactiveMongoConfig
import develop.management.util.cipher.Cipher
import develop.management.util.error.ErrorBody
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

/**
 * Server authorization 테스트 클래스
 * Server 전체를 bind하지 않고 router function만 bind한다
 */
@ActiveProfiles("test")
@SpringBootTest
@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
@Import(TestReactiveMongoConfig::class)
internal class ServiceAuthorizationTest {

    @Autowired
    private lateinit var initializer: ManagementInitializer

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var keyRepository: KeyRepository

    @Autowired
    private lateinit var serviceAuthorizer: ServiceAuthorizer

    private lateinit var authDataData: AuthData

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
            val roomConfig = RoomConfig("test_room", CreateOptions("test_room"))

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

    fun serviceRouter(): RouterFunction<ServerResponse> = coRouter {
        GET("/services") {
            ServerResponse.ok().bodyValueAndAwait("Server authorization success")
        }
        GET("/services/{serviceId}") {
            ServerResponse.ok().bodyValueAndAwait("Server authorization success")
        }
        DELETE("/services/{serviceId}") {
            ServerResponse.ok().bodyValueAndAwait("Server authorization success")
        }
        filter { request, next ->
            request.attributes()["authData"] = authDataData
            next(request)
        }
        filter(serviceAuthorizer::serviceAuthorize)
    }

    /**
     * 전체 service 요청에 대한 인가 테스트
     * @expected: Authorization success (status: 200)
     */

    @Test
    fun superServiceAuthTest() {
        authDataData = AuthData(superService, "auth_test_user", "auth_test_role")
        val result = WebTestClient
                .bindToRouterFunction(serviceRouter())
                .build()
                .get()
                .uri("/services")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * SuperService가 아닌 service id를 이용하였을 때, 전체 service 요청에 대한 인가 테스트
     * @expected: Authorization fail (status: 403, error_code: 1102)
     */
    @Test
    fun notSuperServiceAuthTest() {
        authDataData = AuthData(service, "auth_test_user", "auth_test_role")
        val result = WebTestClient
                .bindToRouterFunction(serviceRouter())
                .build()
                .get()
                .uri("/services")
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
    }

    /**
     * 개별 service에 GET 요청에 대한 인가 테스트
     * @expected: Authorization success (status: 200)
     */
    @Test
    fun normalServiceGetAuthTest() {
        authDataData = AuthData(service, "auth_test_user", "auth_test_role")

        //1. 조회 대상 serviceId를 이용하여 인가
        var result = WebTestClient
                .bindToRouterFunction(serviceRouter())
                .build()
                .get()
                .uri("/services/{serviceId}", service.getId())
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)

        //2. Super serviceId를 이용하여 인가
        authDataData = AuthData(superService, "auth_test_user", "auth_test_role")
        result = WebTestClient
                .bindToRouterFunction(serviceRouter())
                .build()
                .get()
                .uri("/services/{serviceId}", service.getId())
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * auth에 포함되어 있지 않은 개별 service에 GET 요청 인가 테스트
     * @expected: Authorization fail (status: 403, error_code: 1102)
     */
    @Test
    fun normalNotMatchServiceGetAuthTest() {
        authDataData = AuthData(service, "auth_test_user", "auth_test_role")

        val tempServiceId = "not_match_service_auth_test_service@" + Cipher.generateByteArray(1).decodeToString()
        Assertions.assertNotEquals(service.getId(), tempServiceId)

        val result = WebTestClient
                .bindToRouterFunction(serviceRouter())
                .build()
                .get()
                .uri("/services/{serviceId}", tempServiceId)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
    }

    /**
     * 개별 service에 DELETE 요청에 대한 인가 테스트
     * @expected: Authorization success (status: 200)
     */
    @Test
    fun normalServiceDeleteAuthTest() {
        authDataData = AuthData(service, "auth_test_user", "auth_test_role")

        val result = WebTestClient
                .bindToRouterFunction(serviceRouter())
                .build()
                .delete()
                .uri("/services/{serviceId}", service.getId())
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * 일치하지 않는 service에 DELETE 요청에 대한 인가 테스트
     * @expected: Authorization fail (status: 403, error_code: 1102)
     */
    @Test
    fun notMatchServiceDeleteAuthTest() {
        authDataData = AuthData(service, "auth_test_user", "auth_test_role")

        //Auth header에 있는 service id와 다른 serviceId
        val tempServiceId = "not_match_service_auth_test_service@" + Cipher.generateByteArray(1).decodeToString()
        Assertions.assertNotEquals(service, tempServiceId)

        val result = WebTestClient
                .bindToRouterFunction(serviceRouter())
                .build()
                .delete()
                .uri("/services/{serviceId}", tempServiceId)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
    }

    /**
     * Super service에 DELETE 요청에 대한 인가 테스트
     * @expected: Authorization fail (status: 403, error_code: 1102)
     */
    @Test
    fun superServiceDeleteAuthTest() {
        authDataData = AuthData(superService, "auth_test_user", "auth_test_role")
        val result = WebTestClient
                .bindToRouterFunction(serviceRouter())
                .build()
                .delete()
                .uri("/services/{serviceId}", superService.getId())
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
    }
}

