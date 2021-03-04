package develop.management.validator

import com.google.gson.GsonBuilder
import develop.management.domain.dto.PermissionUpdate
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
 * PermissionUpdate validator 테스트 클래스
 */
@SpringBootTest
internal class PermissionUpdateValidatorTest {

    private val validator: PermissionUpdateValidator = PermissionUpdateValidator()

    private lateinit var permissionUpdate: PermissionUpdate

    @BeforeEach
    fun init() {
        permissionUpdate = PermissionUpdate("replace", "/permission/publish/audio", true)
    }

    fun router() = coRouter {
        PATCH("/participants") {
            val update = try { it.awaitBodyOrNull<PermissionUpdate>() } catch (e: Exception) { null } ?: run {
                val error = BadRequestError("Invalid request body: Request body is not valid.")
                return@PATCH ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }

            val errors = BeanPropertyBindingResult(update, PermissionUpdate::class.java.name)
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
     * permission update validator 테스트
     * @expected: Validation success (status: 200)
     */
    @Test
    fun permissionUpdateValidatorTest() {
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .patch()
                .uri("/participants")
                .bodyValue(permissionUpdate)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        println(result.responseBody)
    }

    /**
     * PermissionUpdate가 없을 때 permissionUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noPermssionUpdateValidatorTest() {
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .patch()
                .uri("/participants")
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
     * PermissionUpdate의 op가 null일 때 permissionUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noOpPermissionUpdateValidatorTest() {
        val update = JSONObject()
        update["path"] = "/permission/publish/audio"
        update["value"] = true
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .patch()
                .uri("/participants")
                .bodyValue(update)
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
     * PermissionUpdate의 op가 이상한 값일 때 participantConfig validator 테스트
     * @expected: Invalid request body: Invalid op.  (status: 400, error_code: 1201)
     */
    @Test
    fun invalidOpPermissionUpdateValidatorTest() {
        val update = JSONObject()
        update["op"] = "test"
        update["path"] = "/permission/publish/audio"
        update["value"] = true
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .patch()
                .uri("/participants")
                .bodyValue(update)
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
        Assertions.assertEquals("Invalid request body: Invalid op. ", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * PermissionUpdate의 path가 null일 때 permissionUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid (status: 400, error_code: 1201)
     */
    @Test
    fun noPathPermissionUpdateValidatorTest() {
        val update = JSONObject()
        update["op"] = "replace"
        update["value"] = true
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .patch()
                .uri("/participants")
                .bodyValue(update)
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
     * PermissionUpdate의 path가 유효하지 않을 때 permissionUpdate validator 테스트
     * @expected: Invalid request body: Invalid path. (status: 400, error_code: 1201)
     */
    @Test
    fun invalidPathPermissionUpdateValidatorTest() {
        val update = JSONObject()
        update["op"] = "replace"
        update["path"] = "test"
        update["value"] = true
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .patch()
                .uri("/participants")
                .bodyValue(update)
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
        Assertions.assertEquals("Invalid request body: Invalid path. ", message)
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
    }

    /**
     * PermissionUpdate의 value가 null일 때 permissionUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     * Todo: JSONObject를 이용하여 update를 생성하면 value에 default로 false가 들어간다. 실제 환경에서 테스트해볼 필요가 있다
     */
    /*
    @Test
    fun noValuePermissionUpdateValidatorTest() {
        val update = JSONObject()
        update["op"] = "replace"
        update["path"] = "/permission/publish/audio"
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .patch()
                .uri("/participants")
                .bodyValue(update)
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
    */

    /**
     * PermissionUpdate의 value가 유효하지 않을 때 permissionUpdate validator 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun invalidValuePermissionUpdateValidatorTest() {
        val update = JSONObject()
        update["op"] = "replace"
        update["path"] = "/permission/publish/audio"
        update["value"] = "test"
        val result = WebTestClient
                .bindToRouterFunction(router())
                .build()
                .patch()
                .uri("/participants")
                .bodyValue(update)
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
}

