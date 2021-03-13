package develop.management.validator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import develop.management.domain.dto.StreamingInRequest
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

internal class StreamingInRequestValidatorTest {
    private val validator: StreamingInRequestValidator = StreamingInRequestValidator()

    private lateinit var streamingInRequest: StreamingInRequest

    @BeforeEach
    fun init() {
        streamingInRequest = StreamingInRequest(
            StreamingInRequest.Connection("rtsp://test", "tcp", 8182),
        StreamingInRequest.Media(false, false), null)
    }

    private fun router() = coRouter {
        POST("/streaming-ins") {
            val update = try { it.awaitBodyOrNull<StreamingInRequest>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@POST ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, StreamingInRequest::class.java.name)
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
     * StreamingInRequest validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun streamingInRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
            .bodyValue(streamingInRequest)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * StreamingInRequest 없을 때 streamingInRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noStreamingInRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
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
     * StreamingInRequest의 connection가 null일 때 streamingInRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noConnectionStreamingInReqeuestValidatorTest() {
        val request = JSONObject()
        request["media"] = Gson().toJson(StreamingInRequest.Media(false, false)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
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
     * StreamingInRequest의 media가 null일 때 streamingInRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noMediaStreamingInRequestValidatorTest() {
        val request = JSONObject()
        request["connection"] = Gson().toJson(StreamingInRequest.Connection("rtsp://test", "tcp", 8182)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
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
     * StreamingInRequest connection의 url이 empty일 때 streamingInRequest validator 테스트
     * @expected: Invalid request body: Invalid url.  (status: 400, error_code: 1201)
     */
    @Test
    fun emptyURIStreamingInRequestValidatorTest() {
        val streamingInRequest = StreamingInRequest(
            StreamingInRequest.Connection("", "tcp", 8182),
            StreamingInRequest.Media(false, false), null)

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
            .bodyValue(streamingInRequest)
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

    /**
     * StreamingInRequest connection의 transportProtocol이 올바르지 않을 때 streamingInRequest validator 테스트
     * @expected: Invalid request body: Invalid transportProtocol.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongTransportProtocolStreamingInRequestValidatorTest() {
        val streamingInRequest = StreamingInRequest(
            StreamingInRequest.Connection("rtsp://test", "test", 8182),
            StreamingInRequest.Media(false, false), null)

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
            .bodyValue(streamingInRequest)
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
        assertEquals("Invalid request body: Invalid transportProtocol. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }

    /**
     * StreamingInRequest connection의 bufferSize가 올바르지 않을 때 streamingInRequest validator 테스트
     * @expected: Invalid request body: Invalid bufferSize.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongBufferSizeStreamingInRequestValidatorTest() {
        val streamingInRequest = StreamingInRequest(
            StreamingInRequest.Connection("rtsp://test", "tcp", -1),
            StreamingInRequest.Media(false, false), null)

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
            .bodyValue(streamingInRequest)
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
        assertEquals("Invalid request body: Invalid bufferSize. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }

    /**
     * StreamingInRequest media의 audio가 올바르지 않을 때 streamingInRequest validator 테스트
     * @expected: Invalid request body: Invalid audio.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongAudioStreamingInRequestValidatorTest() {
        val streamingInRequest = StreamingInRequest(
            StreamingInRequest.Connection("rtsp://test", "tcp", 8182),
            StreamingInRequest.Media("test", false), null)

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
            .bodyValue(streamingInRequest)
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
        assertEquals("Invalid request body: Invalid audio. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }

    /**
     * StreamingInRequest media의 video가 올바르지 않을 때 streamingInRequest validator 테스트
     * @expected: Invalid request body: Invalid video.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongVideoStreamingInRequestValidatorTest() {
        val streamingInRequest = StreamingInRequest(
            StreamingInRequest.Connection("rtsp://test", "tcp", 8182),
            StreamingInRequest.Media(false, "test"), null)

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/streaming-ins")
            .bodyValue(streamingInRequest)
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
        assertEquals("Invalid request body: Invalid video. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }
}