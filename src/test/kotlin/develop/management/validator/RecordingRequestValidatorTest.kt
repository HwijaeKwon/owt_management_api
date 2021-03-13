package develop.management.validator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import develop.management.domain.MediaSubOptions
import develop.management.domain.dto.RecordingRequest
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

internal class RecordingRequestValidatorTest {
    private val validator: RecordingRequestValidator = RecordingRequestValidator()

    private lateinit var recordingRequest: RecordingRequest

    @BeforeEach
    fun init() {
        recordingRequest = RecordingRequest("auto", MediaSubOptions(false, false))
    }

    private fun router() = coRouter {
        PATCH("/recordings") {
            val update = try { it.awaitBodyOrNull<RecordingRequest>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@PATCH ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, RecordingRequest::class.java.name)
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
     * RecordingRequest validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun recordingsValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/recordings")
            .bodyValue(recordingRequest)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * RecordingRequest가 없을 때 recordingRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noRecordingRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/recordings")
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
     * RecordingRequest의 container가 null일 때 recordingRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noContainerRecordingsRequestValidatorTest() {
        val request = JSONObject()
        request["media"] = Gson().toJson(MediaSubOptions(false, false)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/recordings")
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
     * RecordingRequest의 media가 null일 때 recordingRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noMediaRecordingRequestValidatorTest() {
        val request = JSONObject()
        request["container"] = "auto"
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/recordings")
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
     * RecordingRequest의 container가 올바르지 않을 때 recordingRequest validator 테스트
     * @expected: Invalid request body: Invalid container.  (status: 400, error_code: 1201)
     */
    @Test
    fun invalidContainerRecordingRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/recordings")
            .bodyValue(RecordingRequest("test", MediaSubOptions(false, false)))
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
        assertEquals("Invalid request body: Invalid container. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }
}