package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.domain.dto.MediaOutControlInfo
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

internal class MediaOutControlInfoValidatorTest {
    private val validator: MediaOutControlInfoValidator = MediaOutControlInfoValidator()

    private lateinit var mediaOutControlInfo: MediaOutControlInfo

    @BeforeEach
    fun init() {
        mediaOutControlInfo = MediaOutControlInfo("replace", "/output/media/video/parameters/from", true)
    }

    private fun router() = coRouter {
        PATCH("/sipcalls") {
            val update = try { it.awaitBodyOrNull<MediaOutControlInfo>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@PATCH ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, MediaOutControlInfo::class.java.name)
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
     * media out control info validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun analyticsValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/sipcalls")
            .bodyValue(mediaOutControlInfo)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * MediaOutControlInfo가 없을 때 mediaOutControlInfo validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noMediaOutControlInfoValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/sipcalls")
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
     * MediaOutControlInfo의 op가 null일 때 mediaOutControlInfo validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noOpMediaOutControlInfoValidatorTest() {
        val request = JSONObject()
        request["path"] = "/output/media/video/parameters/from"
        request["value"] = "true"
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/sipcalls")
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
     * MediaOutControlInfo의 path가 null일 때 mediaOutControlInfo validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noPathMediaOutControlInfoValidatorTest() {
        val request = JSONObject()
        request["op"] = "replace"
        request["value"] = "true"
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/sipcalls")
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
     * MediaOutControlInfo의 value가 null일 때 mediaOutControlInfo validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noValueMediaOutControlInfoValidatorTest() {
        val request = JSONObject()
        request["op"] = "replace"
        request["path"] = "/output/media/video/parameters/from"
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/sipcalls")
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
     * MediaOutControlInfo의 op가 replace가 아닐 때 mediaOutControlInfo validator 테스트
     * @expected: Invalid request body: Invalid op.  (status: 400, error_code: 1201)
     */
    @Test
    fun invalidOpMediaOutControlInfoValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/sipcalls")
            .bodyValue(MediaOutControlInfo("test", "/output/media/video/parameters/from", true))
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

    /**
     * MediaOutControlInfo의 path가 올바르지 않을 때 mediaOutControlInfo validator 테스트
     * @expected: Invalid request body: Invalid op.  (status: 400, error_code: 1201)
     */
    @Test
    fun invalidPathMediaOutControlInfoValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/sipcalls")
            .bodyValue(MediaOutControlInfo("replace", "/output/test", true))
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
        assertEquals("Invalid request body: Invalid path. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }
}