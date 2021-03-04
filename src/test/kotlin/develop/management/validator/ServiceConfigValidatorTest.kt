package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.ServiceConfig
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorBody
import net.minidev.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.util.*

/**
 * ServiceConfig validator 테스트 클래스
 * Authentication, authorization, 다른 validation error는 고려하지 않는다
 * -> Authentication, authorization, 다른 vadlidation error는 따로 테스트 클래스를 만든다
 */
@SpringBootTest
internal class ServiceConfigValidatorTest {

    private val validator: ServiceConfigValidator = ServiceConfigValidator()

    fun router() = coRouter {
        POST("/services") {
            val serviceConfig = try { it.awaitBodyOrNull<ServiceConfig>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Required arguments must not be null")
                return@POST ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(serviceConfig, RoomConfig::class.java.name)
            validator.validate(serviceConfig, errors)
            if(errors.allErrors.isNotEmpty()) {
                var message = "Invalid request body: "
                errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
                val error = BadRequestError(message)
                return@POST ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            ServerResponse.ok().bodyValueAndAwait("Validation success")
        }
    }

    /**
     * serviceConfig validator 테스트
     * @expected: Create service success (status: 200)
     */
    @Test
    fun serviceConfigValidatorTest() {
        val serviceConfig = ServiceConfig("service_config_validator_test", "key")

        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/services")
                .bodyValue(serviceConfig)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * SericeConfig가 올바른 형태가 아닐 때 serviceConfig validator 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun wrongServiceConfigValidatorTest() {
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/services")
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val message = result.responseBody?.error?.message ?: throw AssertionError("Message does not exist")
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals("Invalid request body: Required arguments must not be null", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * SericeConfig의 name이 null 일 때 serviceConfig validator 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun noNameServiceConfigValidatorTest() {
        val serviceConfig = JSONObject()
        serviceConfig["key"] = "key"
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/services")
                .bodyValue(serviceConfig)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val message = result.responseBody?.error?.message ?: throw AssertionError("Message does not exist")
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals( "Invalid request body: Required arguments must not be null", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * SericeConfig의 name이 empty 일 때 serviceConfig validator 테스트
     * @expected: Invalid request body: The name must not be empty.  (status: 400, error_code: 1201)
     */
    @Test
    fun emptyNameServiceConfigValidatorTest() {
        val serviceConfig = ServiceConfig("", "key")
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/services")
                .bodyValue(serviceConfig)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val message = result.responseBody?.error?.message ?: throw AssertionError("Message does not exist")
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals("Invalid request body: The name must not be empty. ", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * SericeConfig의 key가 null 일 때 serviceConfig validator 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun noKeyServiceConfigValidatorTest() {
        val serviceConfig = JSONObject()
        serviceConfig["name"] = "service_config_validator_test"
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/services")
                .bodyValue(serviceConfig)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val message = result.responseBody?.error?.message ?: throw AssertionError("Message does not exist")
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals("Invalid request body: Required arguments must not be null", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * SericeConfig의 key가 empty 일 때 serviceConfig validator 테스트
     * @expected: Invalid request body: The key must not be empty.  (status: 400, error_code: 1201)
     */
    @Test
    fun emptyKeyServiceConfigValidatorTest() {
        val serviceConfig = ServiceConfig("service_config_validator_test", "")
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/services")
                .bodyValue(serviceConfig)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val message = result.responseBody?.error?.message ?: throw AssertionError("Message does not exist")
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        Assertions.assertEquals("Invalid request body: The key must not be empty. ", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }
}

