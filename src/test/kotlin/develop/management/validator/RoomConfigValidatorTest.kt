package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.domain.dto.CreateOptions
import develop.management.domain.dto.RoomConfig
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorBody
import net.minidev.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.*
import java.util.*

/**
 * RoomConfig validator 테스트 클래스
 */
internal class RoomConfigValidatorTest {

    private val validator: RoomConfigValidator = RoomConfigValidator()

    private lateinit var options: CreateOptions

    @BeforeEach
    fun init() {
        options = CreateOptions()
    }

    fun router() = coRouter {
        POST("/rooms") {
            val roomConfig = try { it.awaitBodyOrNull<RoomConfig>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@POST ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(roomConfig, RoomConfig::class.java.name)
            validator.validate(roomConfig, errors)
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
     * roomConfig validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun roomConfigValidatorTest() {
        val roomConfig = RoomConfig("name", options)
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/rooms")
                .bodyValue(roomConfig)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * RoomConfig가 없을 때 roomConfig validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noRoomConfigValidatorTest() {
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/rooms")
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
        Assertions.assertEquals("Invalid request body: Request body is not valid.", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * RoomConfig가 올바른 형태가 아닐 때 roomConfig validator 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun wrongRoomConfigValidatorTest() {
        val roomConfig = JSONObject()
        roomConfig["name"] = "name"
        roomConfig["options"] = JSONObject().also {
            it["views"] = "views"
        }
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/rooms")
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
        Assertions.assertEquals("Invalid request body: Request body is not valid.", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * RoomConfig의 hostKey가 null일 때 roomConfig validator 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun noHostKeyRoomConfigValidatorTest() {
        val roomConfig = JSONObject()
        roomConfig["options"] = JSONObject().also { it["name"] = "name" }
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/rooms")
                .bodyValue(roomConfig)
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
        Assertions.assertEquals("Invalid request body: Request body is not valid.", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * RoomConfig의 options가 null일 때 roomConfig validator 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun noOptionsRoomConfigValidatorTest() {
        val roomConfig = JSONObject()
        roomConfig["name"] = "name"
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/rooms")
                .bodyValue(roomConfig)
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
        Assertions.assertEquals("Invalid request body: Request body is not valid.", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * RoomConfig의 name이 null일 때 roomConfig validator 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun noNameRoomConfigValidatorTest() {
        val roomConfig = JSONObject()
        roomConfig["options"] = JSONObject()

        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/rooms")
                .bodyValue(roomConfig)
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
        Assertions.assertEquals("Invalid request body: Request body is not valid.", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }


    /**
     * RoomConfig의 name이 empty일 때 roomConfig validator 테스트
     * @expected: Invalid request body: The name must not be empty. (status: 400, error_code: 1201)
     */
    @Test
    fun emptyNameRoomConfigValidatorTest() {
        val roomConfig = RoomConfig("", CreateOptions())
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/rooms")
                .bodyValue(roomConfig)
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
}

