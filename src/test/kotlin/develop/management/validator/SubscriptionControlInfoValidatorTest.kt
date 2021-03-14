package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.domain.dto.SubscriptionControlInfo
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

internal class SubscriptionControlInfoValidatorTest {
    private val validator: SubscriptionControlInfoValidator = SubscriptionControlInfoValidator()

    private val subscriptionControlInfo = SubscriptionControlInfo("replace", "/media/audio/from", "/media/video/from")

    private fun router() = coRouter {
        PATCH("/recordings") {
            val update = try { it.awaitBodyOrNull<SubscriptionControlInfo>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@PATCH ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, SubscriptionControlInfo::class.java.name)
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
     * SubscriptionControlInfo validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun subscriptionControlInfoValidatorTest() {
        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/recordings")
            .bodyValue(subscriptionControlInfo)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .returnResult()

        assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * SubscriptionControlInfo가 없을 subscriptionControlInfo validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noSubscriptionControlInfoValidatorTest() {
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
     * SubscriptionControlInfo의 op가 null일 때 subscriptionControlInfo validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noOpSubscriptionControlInfoValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["path"] = "/media/audio/from"
        request["value"] = "/media/audio/from"
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
     * SubscriptionControlInfo의 path가 null일 때 subscriptionControlInfo validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noPathSubscriptionControlInfoValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["op"] = "replace"
        request["value"] = "/media/audio/from"
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
     * SubscriptionControlInfo의 value가 null일 때 subscriptionControlInfo validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noValueSubscriptionControlInfoValidatorTest() {
        val request = net.minidev.json.JSONObject()
        request["op"] = "add"
        request["value"] = "/media/audio/from"
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
     * SubscriptionControlInfo의 op가 올바르지 않을 때 subscriptionControlInfo validator 테스트
     * @expected: Invalid request body: Invalid op.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongOpSubscriptionControlInfoValidatorTest() {
        val subscriptionControlInfo = SubscriptionControlInfo("test", "/media/audio/from", "/media/audio/from")

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/recordings")
            .bodyValue(subscriptionControlInfo)
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
     * SubscriptionControlInfo의 path가 올바르지 않을 때 subscriptionControlInfo validator 테스트
     * @expected: Invalid request body: Invalid path.  (status: 400, error_code: 1201)
     */
    @Test
    fun wrongPathSubscriptionControlInfoValidatorTest() {
        val subscriptionControlInfo = SubscriptionControlInfo("replace", "test", "/media/audio/from")

        val result = WebTestClient
            .bindToRouterFunction(router())
            .build()
            .patch()
            .uri("/recordings")
            .bodyValue(subscriptionControlInfo)
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