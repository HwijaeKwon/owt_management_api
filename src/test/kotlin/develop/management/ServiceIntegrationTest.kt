package develop.management

import com.google.gson.GsonBuilder
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
import net.minidev.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.util.*

/**
 * Service 관련 요청 테스트 클래스
 * Authentication, authorization, validation error는 고려하지 않는다
 * -> Authentication, authorization, vadlidation error는 따로 테스트 객체를 만든다
 */
@SpringBootTest(classes = [DevelopApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
@Import(TestReactiveMongoConfig::class)
class ServiceIntegrationTest {

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var keyRepository: KeyRepository

    @Autowired
    private lateinit var initializer: ManagementInitializer

    @LocalServerPort
    private var port: Int = 0

    private lateinit var auth: String

    private lateinit var superAuth: String

    private lateinit var service: Service

    @BeforeEach
    fun init() = runBlocking {
        initializer.init()

        if(!serviceRepository.existsById(initializer.getSuperServiceId())) throw AssertionError("SuperService dose not exist")
        val serviceName = "service_integration_test@" + Cipher.generateByteArray(1).decodeToString()
        val key = Cipher.generateKey(128, "HmacSHA256")
        if(serviceRepository.existsByName(serviceName)) throw AssertionError("Service already exists")
        service = serviceRepository.save(Service.create(ServiceConfig(serviceName, key)))
        serviceRepository.save(service)

        val roomName = "service_integration_test@" + Cipher.generateByteArray(1).decodeToString()
        val options = CreateOptions(roomName)
        val room = roomRepository.save(Room.create(RoomConfig(roomName, options)))
        service.addRoom(room.getId())
        service = serviceRepository.save(service)

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

        val superSignature = Cipher.createHmac(message, initializer.getSuperServiceKey(), "HmacSHA256")
        superAuth = "MAuth realm=http://marte3.dit.upm.es," +
                "mauth_signature_method=HMAC_SHA256," +
                "mauth_serviceid=" + initializer.getSuperServiceId() + "," +
                "mauth_cnonce=" + cnonce + "," +
                "mauth_timestamp=" + timestamp + "," +
                "mauth_signature=" + superSignature
    }

    @AfterEach
    fun close() {
        runBlocking {
            serviceRepository.deleteAll()
            roomRepository.deleteAll()
            keyRepository.deleteAll()
        }
    }

    /** 1. Authentication 테스트 **/
    //Authentication unit test


    /** 2. Authorization 테스트 **/
    /**
     * SuperService가 아닌 service로 새로운 service 생성 요청 테스트
     * @expected: Permission denied (status: 403, code: 1102)
     */
    @Test
    fun createWithNotSuperServiceTest() {
        val serviceName = "create_test_service@" + Cipher.generateByteArray(1).toString()
        if( runBlocking { serviceRepository.existsByName(serviceName) }) throw AssertionError("Service already exists")
        val key = Cipher.generateKey(32, "HmacSHA256")
        val serviceConfig = ServiceConfig(serviceName, key)
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .bodyValue(serviceConfig)
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
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
        Assertions.assertEquals("Permission denied", message)
    }

    /**
     * SuperService가 아닌 service로 모든 service 조회 요청 테스트
     * @expected : Permission denied (status: 403, code: 1102)
     */
    @Test
    fun findAllWithNoSuperServiceTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/services")
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
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
        Assertions.assertEquals("Permission denied", message)
    }

    /**
     * Request에 header에 있는 service와 다른 service 조회 요청 테스트
     * @expected: Permission denied (status: 403, error_code: 1102)
     */
    @Test
    fun findAnotherServiceTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/services/{serviceId}", initializer.getSuperServiceId())
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
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
        Assertions.assertEquals("Permission denied", message)
    }

    /**
     * Request에 header에 있는 service와 다른 service 삭제 요청 테스트
     * @expected: Permission denied (status: 403, error_code: 1102)
     */
    @Test
    fun deleteAnotherServiceTest() {
        val serviceName = "another_service@" + Cipher.generateByteArray(1).decodeToString()
        val anotherService = runBlocking {
            if(serviceRepository.existsByName(serviceName)) throw AssertionError("Service already exists")
            serviceRepository.save(Service.create(ServiceConfig(serviceName, "key")))
        }
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .delete()
                .uri("/services/{serviceId}", anotherService.getId())
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
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
        Assertions.assertEquals("Permission denied", message)
    }

    /**
     * SuperService 삭제 요청 테스트
     * @expected: Permission denied: Super service deletion is not permitted (status: 403, error_code: 1102)
     */
    @Test
    fun deleteSuperServiceTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .delete()
                .uri("/services/{serviceId}", initializer.getSuperServiceId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
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
        Assertions.assertEquals(403, status)
        Assertions.assertEquals(1102, code)
        Assertions.assertEquals("Permission denied: Super service deletion is not permitted", message)
    }

    /** 3. Validation 테스트 **/

    /**
     * ServiceConfig를 전달하지 않았을 때 서비스 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun createNoServiceConfigTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
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

    /**
     * 올바르지 않은 serviceConfig를 전달했을때 서비스 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun createWrongServiceConfigTest() {
        val serviceConfig = "test"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .bodyValue(serviceConfig)
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

    /**
     * ServiceConfig의 name이 null일 때 서비스 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun createWithNoNameServiceConfigTest() {
        val serviceConfig = JSONObject()
        serviceConfig["key"] = "key"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .bodyValue(serviceConfig)
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

    /**
     * ServiceConfig의 name이 empty일 때 서비스 생성 요청 테스트
     * @expected: Invalid request body: The name must not be empty. (status: 400, error_code: 1201)
     */
    @Test
    fun createWithEmptyNameServiceConfigTest() {
        val serviceConfig = ServiceConfig("", "key")
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .bodyValue(serviceConfig)
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
        Assertions.assertEquals("Invalid request body: The name must not be empty. ", message)
    }

    /**
     * ServiceConfig의 key가 null일 때 서비스 생성 요청 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun createWithNoKeyServiceConfigTest() {
        val serviceConfig = JSONObject()
        serviceConfig["name"] = "name"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .bodyValue(serviceConfig)
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

    /**
     * ServiceConfig의 key가 empty일 때 서비스 생성 요청 테스트
     * @expected: Invalid request body: The key must not be empty. (status: 400, error_code: 1201)
     */
    @Test
    fun createWithEmptyKeyServiceConfigTest() {
        val serviceConfig = ServiceConfig("service_integration_test", "")
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .bodyValue(serviceConfig)
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
        Assertions.assertEquals("Invalid request body: The key must not be empty. ", message)
    }

    /** 4. Service 테스트 **/

    /**
     * 새로운 service 생성 요청 테스트
     * @expected: DB에 서비스가 생성되고, 생성된 서비스를 반환받는다
     */
    @Test
    fun createTest() {
        val serviceName = "create_test_service@" + Cipher.generateByteArray(1).toString()
        runBlocking {
            if(serviceRepository.existsByName(serviceName)) throw AssertionError("Service already exists")
        }
        val key = Cipher.generateKey(32, "HmacSHA256")
        val serviceConfig = ServiceConfig(serviceName, key)
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .bodyValue(serviceConfig)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(Service::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val savedServiceName = result.responseBody!!.getName()
        Assertions.assertEquals(serviceName, savedServiceName)
        runBlocking {
            if(!serviceRepository.existsByName(serviceName)) throw AssertionError("Service dose not exists")
        }
    }

    /**
     * 중복되는 service 생성 요청 테스트
     * @expected: Create service fail: Service already exists (status: 500, error_code : 2001)
     */
    @Test
    fun createDuplicatedServiceTest() {
        val serviceName = service.getName()
        val key = service.getKey()
        val serviceConfig = ServiceConfig(serviceName, key)
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .bodyValue(serviceConfig)
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
        Assertions.assertEquals("Create service fail: Service already exists", message)
    }

    /**
     * 모든 service를 조회하는 테스트
     * @expected : DB에 있는 모든 service의 리스트를 받는다
     */
    @Test
    fun findAllTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/services")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList<Service>()
                .returnResult()

        val serviceList = runBlocking { serviceRepository.findAll() }
        val gson = GsonBuilder().setPrettyPrinting().create()
        result.responseBody?.forEach {
            //Todo: ServiceData에서 id가 lateinit으로 정의되어 있어 id는 출력되지 않는다
            // id를 출력하려면, String이나 JSONObject로 받아야 한다
            // Dto를 도입할 필요학 있다
            println(gson.toJson(it))
        }
        Assertions.assertNotNull(result.responseBody)
        Assertions.assertEquals(serviceList.size, result.responseBody!!.size)
    }

    /**
     * SuperService로 개별 서비스 조회 요청 테스트
     * @expected: 조회한 서비스를 반환한다
     */
    @Test
    fun findOneWithSuperServiceTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/services/{serviceId}", service.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(Service::class.java)
                .returnResult()

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        Assertions.assertNotNull(result.responseBody)
        Assertions.assertEquals(service.getName(), result.responseBody!!.getName())
        runBlocking {
            if(!serviceRepository.existsByName(result.responseBody!!.getName())) throw AssertionError("Service does not exists")
        }
    }

    /**
     * SuperService가 아닌 service로 해당 서비스 조회 요청 테스트
     * @expected: 조회한 서비스를 반환한다
     */
    @Test
    fun findOneWithNotSuperServiceTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/services/{serviceId}", service.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(Service::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        Assertions.assertEquals(service.getName(), result.responseBody!!.getName())
        runBlocking {
            if(!serviceRepository.existsByName(result.responseBody!!.getName())) throw AssertionError("Service does not exists")
        }
    }

    /**
     * 존재하지 않는 service 조회 요청 테스트
     * @expected: Service not found (status: 404, error_code: 1002)
     */
    @Test
    fun findNotExistServiceTest() {
        val serviceId = "not_exist_service"
        runBlocking {
            if(serviceRepository.existsById(serviceId)) throw AssertionError("Service already exists")
        }
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/services/{serviceId}", serviceId)
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
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
        Assertions.assertEquals(1002, code)
        Assertions.assertEquals("Service not found", message)
    }

    /**
     * SuperService를 이용하여 service 삭제 요청 테스트
     * @expected: Service deleted 메세지를 반환한다
     */
    @Test
    fun deleteWithSuperServiceTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .delete()
                .uri("/services/{serviceId}", service.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertEquals("Service deleted", result.responseBody)
        println(result.responseBody)
        runBlocking {
            if(serviceRepository.existsByName(service.getId())) throw AssertionError("Service still exists")
        }
    }

    /**
     * SuperService가 아닌 service를 이용하여 service 삭제 요청 테스트
     * @expected: Service deleted 메세지를 반환한다
     */
    @Test
    fun deleteWithNotSuperServiceTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .delete()
                .uri("/services/{serviceId}", service.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertEquals("Service deleted", result.responseBody)
        println(result.responseBody)
        runBlocking {
            if(serviceRepository.existsByName(service.getId())) throw AssertionError("Service still exists")
        }
    }

    /**
     * 존재하지 않는 service 삭제 요청 테스트
     * @expected: Service not found (status: 404, error_code: 1002)
     */
    @Test
    fun deleteNotExistServiceTest() {
        val serviceId = "not_exist_service"
        if(runBlocking { serviceRepository.existsById(serviceId)}) throw AssertionError("Service already exists")

        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .delete()
                .uri("/services/{serviceId}", serviceId)
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", superAuth)
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
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1002, code)
        Assertions.assertEquals("Service not found", message)
    }
}