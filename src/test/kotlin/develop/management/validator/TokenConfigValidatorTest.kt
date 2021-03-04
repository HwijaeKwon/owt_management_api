package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.TokenConfig
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
 * TokenRequest validator 테스트 클래스
 */
@SpringBootTest
internal class TokenConfigValidatorTest {

    private val validator: TokenConfigValidator = TokenConfigValidator()

    fun router() = coRouter {
        POST("/tokens") {
            val tokenRequest = try { it.awaitBodyOrNull<TokenConfig>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Required arguments must not be null")
                return@POST ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(tokenRequest, RoomConfig::class.java.name)
            validator.validate(tokenRequest, errors)
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
     * tokenRequest validator 테스트
     * @expected: Create token success (status: 200)
     */
    @Test
    fun tokenConfigValidatorTest() {
        val tokenRequest = TokenConfig("token_config_validator_test_user", "role")

        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/tokens")
                .bodyValue(tokenRequest)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * TokenRequest가 올바른 형태가 아닐 때 tokenRequest validator 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun wrongTokenConfigValidatorTest() {
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/tokens")
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
     * TokenRequest의 user가 null 일 때 tokenRequest validator 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun noUserTokenRequestValidatorTest() {
        val tokenRequest = JSONObject()
        tokenRequest["role"] = "presenter"
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/tokens")
                .bodyValue(tokenRequest)
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
        Assertions.assertEquals( 400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * TokenRequest의 user가 empty 일 때 tokenRequest validator 테스트
     * @expected: Invalid request body: The user must not be empty.  (status: 400, error_code: 1201)
     */
    @Test
    fun emptyUserTokenRequestValidatorTest() {
        val tokenRequest = TokenConfig("", "presenter")
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/tokens")
                .bodyValue(tokenRequest)
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
        Assertions.assertEquals( "Invalid request body: The user must not be empty. ", message)
        Assertions.assertEquals( 400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * TokenRequest의 role이 null 일 때 tokenRequest validator 테스트
     * @expected: Invalid request body: Required arguments must not be null (status: 400, error_code: 1201)
     */
    @Test
    fun noRoleTokenRequestValidatorTest() {
        val tokenRequest = JSONObject()
        tokenRequest["user"] = "token_request_validator_test_user"
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/tokens")
                .bodyValue(tokenRequest)
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
     * TokenRequest의 role이 empty 일 때 tokenRequest validator 테스트
     * @expected: Invalid request body: The role must not be empty. (status: 400, error_code: 1201)
     */
    @Test
    fun emptyRoleTokenRequestValidatorTest() {
        val tokenRequest = TokenConfig("token_request_validator_test_user", "")
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .post()
                .uri("/tokens")
                .bodyValue(tokenRequest)
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
        Assertions.assertEquals("Invalid request body: The role must not be empty. ", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }
}

