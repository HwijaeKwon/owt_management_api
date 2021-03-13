package develop.management.validator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import develop.management.domain.*
import develop.management.domain.dto.SipCallRequest
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

internal class SipCallRequestValidatorTest {
    private val validator: SipCallRequestValidator = SipCallRequestValidator()

    private lateinit var sipCallRequest: SipCallRequest

    @BeforeEach
    fun init() {
        sipCallRequest = SipCallRequest("peerURI",
        SipCallRequest.MediaIn(true, true), SipOutMedia(SipOutMedia.SipOutAudio("audio"), true))
    }

    private fun router() = coRouter {
        POST("/sipcalls") {
            val update = try { it.awaitBodyOrNull<SipCallRequest>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@POST ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, SipCallRequest::class.java.name)
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
     * SipCallRequest validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun sipCallRequestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/sipcalls")
            .bodyValue(sipCallRequest)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * SipCallRequest이 없을 때 sipCallReuqest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noSipCallReuqestValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
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
     * SipCallRequest의 peerURI이 null일 때 sipCallRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noPeerURISipCallRequestValidatorTest() {
        val request = JSONObject()
        request["mediaIn"] = Gson().toJson(SipCallRequest.MediaIn(true, true)).toString()
        request["mediaOut"] = Gson().toJson(SipOutMedia(SipOutMedia.SipOutAudio("audio"), true)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
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
     * SipCallRequest의 mediaIn이 null일 때 sipCallRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noMediaInSipCallRequestValidatorTest() {
        val request = JSONObject()
        request["peerURI"] = "peerURI"
        request["mediaOut"] = Gson().toJson(SipOutMedia(SipOutMedia.SipOutAudio("audio"), true)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
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
     * SipCallRequest의 mediaOut이 null일 때 sipCallRequest validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noMediaOutSipCallRequestValidatorTest() {
        val request = JSONObject()
        request["peerURI"] = "peerURI"
        request["mediaIn"] = Gson().toJson(SipCallRequest.MediaIn(true, true)).toString()
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
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
     * SipCallRequest의 peerURI가 empty일 때 sipCallRequest validator 테스트
     * @expected: Invalid request body: Invalid peerURI.  (status: 400, error_code: 1201)
     */
    @Test
    fun emptyPeerURISipCallRequestValidatorTest() {
        val sipCallRequest = SipCallRequest("",
            SipCallRequest.MediaIn(true, true), SipOutMedia(SipOutMedia.SipOutAudio("audio"), true))

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .post()
            .uri("/sipcalls")
            .bodyValue(sipCallRequest)
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
        assertEquals("Invalid request body: Invalid peerURI. ", message)
        assertEquals(400, status)
        assertEquals(1201, code)
    }
}