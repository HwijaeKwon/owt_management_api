package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.domain.dto.StreamUpdate
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

internal class StreamUpdateValidatorTest {
    private val validator: StreamUpdateValidator = StreamUpdateValidator()

    private val streamUpdate = StreamUpdate("add", "/info/inViews", "id")

    private fun router() = coRouter {
        PATCH("/streams") {
            val update = try { it.awaitBodyOrNull<StreamUpdate>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@PATCH ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, StreamUpdate::class.java.name)
            validator.validate(update, errors)
            if(errors.allErrors.isNotEmpty()) {
                var message = "Invalid request body: "
                errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
                val error = BadRequestError(message)
                return@PATCH ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            ServerResponse.ok().bodyValueAndAwait("Validation success")
        }
    }

    /**
     * StreamUpdate validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun streamUpdateValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/streams")
            .bodyValue(streamUpdate)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * StreamUpdate가 없을 때 streamUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noStreamUpdateValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/streams")
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
     * StreamUpdate의 op가 null일 때 streamUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noOpStreamUpdateValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["path"] = "/info/inViews"
        request["value"] = "id"
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/streams")
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
     * StreamUpdate의 path가 null일 때 streamUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noPathStreamUpdateValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["op"] = "add"
        request["value"] = "id"
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/streams")
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
     * StreamUpdate의 value가 null일 때 streamUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noValueStreamUpdateValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["op"] = "add"
        request["path"] = "/info/inViews"
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/streams")
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
     * StreamUpdate의 op가 올바르지 않을 때 streamUpdate validator 테스트
     * @expected: Invalid request body: Invalid op.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongOpStreamUpdateValidatorTest() {
        val streamUpdate = StreamUpdate("test", "/info/inViews", "id")

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/streams")
            .bodyValue(streamUpdate)
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
        assertEquals("Invalid request body: Invalid op. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }
}