package develop.management.validator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import develop.management.domain.MediaSubOptions
import develop.management.domain.dto.AnalyticsRequest
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorBody
import net.minidev.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

internal class AnalyticsRequestValidatorTest {
    private val validator: AnalyticsRequestValidator = AnalyticsRequestValidator()

    private lateinit var analyticsRequest: AnalyticsRequest

    @BeforeEach
    fun init() {
        analyticsRequest = AnalyticsRequest("algorithm", MediaSubOptions(false, false))
    }

    private fun router() = coRouter {
        POST("/analytics") {
            val update = try { it.awaitBodyOrNull<AnalyticsRequest>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@POST ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, AnalyticsRequest::class.java.name)
            validator.validate(update, errors)
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
     * analytics request validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun analyticsValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/analytics")
            .bodyValue(analyticsRequest)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * AnalyticsRequest가 없을 때 analyticsRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noAnalyticsRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/analytics")
            .exchange()
            .expectStatus()
            .is4xxClientError
            .expectBody(ErrorBody::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val message = result.responseBody?.error?.message ?: throw AssertionError("Message does not exist")
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        assertEquals("Invalid request body: Request body is not valid.", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }

    /**
     * AnalyticsRequest의 algorithm이 null일 때 analyticsRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noAlgorithmAnalyticsRequestValidatorTest() {
        val request = JSONObject()
        request["media"] = Gson().toJson(MediaSubOptions(false, false)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/analytics")
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .is4xxClientError
            .expectBody(ErrorBody::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val message = result.responseBody?.error?.message ?: throw AssertionError("Message does not exist")
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        assertEquals("Invalid request body: Request body is not valid.", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }

    /**
     * AnalyticsRequest의 algorithm이 empty일 때 analyticsRequest validator 테스트
     * @expected: Invalid request body: Invalid algorithm.  (status: 400, error_code: 1201)
     */
    @Test
    fun invalidAlgorithmAnalyticsRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/analytics")
            .bodyValue(AnalyticsRequest("", MediaSubOptions(false, false)))
            .exchange()
            .expectStatus()
            .is4xxClientError
            .expectBody(ErrorBody::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val message = result.responseBody?.error?.message ?: throw AssertionError("Message does not exist")
        val status = result.status.value()
        val code = result.responseBody?.error?.code ?: throw AssertionError("Code does not exist")
        assertEquals("Invalid request body: Invalid algorithm. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }
}