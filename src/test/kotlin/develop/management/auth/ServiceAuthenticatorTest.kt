package develop.management.auth

import com.google.gson.GsonBuilder
import develop.management.domain.document.Service
import develop.management.domain.dto.ServiceConfig
import develop.management.repository.ServiceRepository
import develop.management.repository.mongo.TestReactiveMongoConfig
import develop.management.util.cipher.Cipher
import develop.management.util.error.ErrorBody
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
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
@Import(TestReactiveMongoConfig::class)
internal class ServiceAuthenticatorTest {

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var serviceAuthenticator: ServiceAuthenticator

    private lateinit var serviceId: String

    private lateinit var serviceKey: String

    private lateinit var auth: String

    fun router(): RouterFunction<ServerResponse> = coRouter {
        GET("") {
            val authData = it.attributes()["authData"] as ServiceAuthenticator.AuthData
            ServerResponse.ok().bodyValueAndAwait(authData)
        }
        filter(serviceAuthenticator::authenticate)
    }

    @BeforeEach
    fun init() {
        val serviceName = "auth_test_service@" + Cipher.generateByteArray(1).decodeToString()
        runBlocking {
            if (serviceRepository.existsByName(serviceName)) throw AssertionError("Service already exists")
            serviceKey = Cipher.generateKey(128, "HmacSHA256")
            val serviceData = Service.create(ServiceConfig(serviceName, serviceKey))
            serviceId = serviceRepository.save(serviceData).getId()
        }
    }

    @AfterEach
    fun close() {
        runBlocking { serviceRepository.deleteById(serviceId) }
    }

    /**
     * 올바른 auth 정보를 전달했을 때 server 인증 테스트
     * @expected: Authenticate success (status: 200)
     */
    @Test
    fun correctAuthTest() {
        //SuperServiceId 혹은 client가 사용할 service id
        val authServiceId = serviceId

        //SuperServiceKey 혹은 client가 사용할 service key
        val cnonce = "cnonce"
        val timestamp = Date().toString()
        val message = "$timestamp,$cnonce"
        val signature = Cipher.createHmac(message, serviceKey, "HmacSHA256")
        auth = "MAuth realm=http://marte3.dit.upm.es," +
                "mauth_signature_method=HMAC_SHA256," +
                "mauth_serviceid=" + authServiceId + "," +
                "mauth_cnonce=" + cnonce + "," +
                "mauth_timestamp=" + timestamp + "," +
                "mauth_signature=" + signature

        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .get()
                .uri("")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", auth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val jsonResult = JSONObject(result.responseBody)
        println(jsonResult.toString(4))
        val service = jsonResult.getJSONObject("service")?: throw AssertionError("Service not exist")
        Assertions.assertEquals(service.getString("id"), authServiceId)
    }

    /**
     * Authorization 정보를 전달하지 않았을 때 server 인증 테스트
     * @expected: Authenticate fail (status: 401, error_code: 1101)
     */
    @Test
    fun noAuthHeaderTest() {
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .get()
                .uri("")
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
        Assertions.assertEquals(401, status)
        Assertions.assertEquals(1101, code)
    }

    /**
     * serviceId를 포함하지 않았을 때 server 인증 테스트
     * @expected: Authenticate fail (status: 401, error_code: 1101)
     */
    @Test
    fun noServiceIdAuthTest() {
        val cnonce = "cnonce"
        val timestamp = Date().toString()
        val message = "$timestamp,$cnonce"
        val signature = Cipher.createHmac(message, serviceKey, "HmacSHA256")
        auth = "MAuth realm=http://marte3.dit.upm.es," +
                "mauth_signature_method=HMAC_SHA256," +
                "mauth_cnonce=" + cnonce + "," +
                "mauth_timestamp=" + timestamp + "," +
                "mauth_signature=" + signature

        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .get()
                .uri("")
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
        Assertions.assertEquals(401, status)
        Assertions.assertEquals(1101, code)
    }

    /**
     * 존재하지 않는 serviceId를 전달했을 때 server 인증 테스트
     * @expected: Authenticate fail (status: 401, error_code: 1101)
     */
    @Test
    fun notExistServiceIdAuthTest() {
        //임의의 serviceId
        val notExistServiceId = "not_exist_serviceId@" + Cipher.generateByteArray(1).toString()
        if(runBlocking { serviceRepository.existsById(notExistServiceId) }) throw AssertionError("Service already exists")

        val cnonce = "cnonce"
        val timestamp = Date().toString()
        val message = "$timestamp,$cnonce"
        val signature = Cipher.createHmac(message, serviceKey, "HmacSHA256")
        auth = "MAuth realm=http://marte3.dit.upm.es," +
                "mauth_signature_method=HMAC_SHA256," +
                "mauth_serviceid=" + notExistServiceId + "," +
                "mauth_cnonce=" + cnonce + "," +
                "mauth_timestamp=" + timestamp + "," +
                "mauth_signature=" + signature

        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .get()
                .uri("")
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
        Assertions.assertEquals(401, status)
        Assertions.assertEquals(1101, code)
    }

    /**
     * Signature 비교 실패 테스트
     * @expected: Authenticate fail (status: 401, error_code: 1101)
     */
    @Test
    fun wrongSignatureAuthTest() {
        //SuperServiceId 혹은 client가 사용할 service id
        val authServiceId = serviceId

        //임의로 만든 key
        val tempKey = Cipher.generateKey(128, "HmacSHA256")
        val cnonce = "cnonce"
        val timestamp = Date().toString()
        val message = "$timestamp,$cnonce"
        val signature = Cipher.createHmac(message, tempKey, "HmacSHA256")
        auth = "MAuth realm=http://marte3.dit.upm.es," +
                "mauth_signature_method=HMAC_SHA256," +
                "mauth_serviceid=" + authServiceId + "," +
                "mauth_cnonce=" + cnonce + "," +
                "mauth_timestamp=" + timestamp + "," +
                "mauth_signature=" + signature

        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .get()
                .uri("")
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
        Assertions.assertEquals(401, status)
        Assertions.assertEquals(1101, code)
    }
}
