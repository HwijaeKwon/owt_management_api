package develop.management.validator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import develop.management.domain.HlsParameters
import develop.management.domain.MediaSubOptions
import develop.management.domain.dto.StreamingOutRequest
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

internal class StreamingOutRequestValidatorTest {
    private val validator: StreamingOutRequestValidator = StreamingOutRequestValidator()

    private val streamingOutRequest = StreamingOutRequest("rtmp", "url", HlsParameters("PUT", 10, 10), MediaSubOptions(false, false))

    private fun router() = coRouter {
        POST("/streaming-outs") {
            val update = try { it.awaitBodyOrNull<StreamingOutRequest>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@POST ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, StreamingOutRequest::class.java.name)
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
     * StreamingOutRequest validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun streamingInRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-outs")
            .bodyValue(streamingOutRequest)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * StreamingOutRequest 없을 때 streamingOutRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noStreamingOutRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-outs")
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
     * StreamingOutRequest의 protocol이 null일 때 streamingOutRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noProtocolStreamingOutRequestValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["url"] = "url"
        request["media"] = Gson().toJson(MediaSubOptions(false, false)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-outs")
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
     * StreamingOutRequest의 url가 null일 때 streamingOutRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noUrlStreamingOutRequestValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["protocol"] = "rtmp"
        request["media"] = Gson().toJson(MediaSubOptions(false, false)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-outs")
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
     * StreamingOutRequest의 media가 null일 때 streamingOutRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noMediaStreamingOutRequestValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["protocol"] = "rtmp"
        request["url"] = "url"
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-outs")
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
     * StreamingOutRequest protocol이 올바르지 않을 때 streamingOutRequest validator 테스트
     * @expected: Invalid request body: Invalid protocol.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongProtocolStreamingOutRequestValidatorTest() {
        val streamingOutRequest = StreamingOutRequest("test", "url", null, MediaSubOptions(false, false))

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-outs")
            .bodyValue(streamingOutRequest)
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
        assertEquals("Invalid request body: Invalid protocol. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }

    /**
     * StreamingOutRequest url가 empty streamingOutRequest validator 테스트
     * @expected: Invalid request body: Invalid url.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongTransportProtocolStreamingInRequestValidatorTest() {
        val streamingOutRequest = StreamingOutRequest("rtmp", "", null, MediaSubOptions(false, false))

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-outs")
            .bodyValue(streamingOutRequest)
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
        assertEquals("Invalid request body: Invalid url. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }
}