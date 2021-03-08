package develop.management.handler

import develop.management.auth.ServiceAuthenticator
import develop.management.domain.document.Token
import develop.management.domain.dto.TokenConfig
import develop.management.service.TokenService
import develop.management.util.error.AppError
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorFoam
import develop.management.util.error.NotFoundError
import develop.management.validator.TokenConfigValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * Token 관련된 요청을 처리하는 handler function 모음
 * Token model : https://software.intel.com/sites/products/documentation/webrtc/restapi/의 token 참고 (자사 documentation 필요함)
 */
@Component
class TokenHandler(private val tokenService: TokenService) {

    /**
     * 특정 service의 특정 room을 위한 token을 생성한다
     * Todo: host를 request에서 직접 얻어서 사용하도록 수정해야한다
     */
    @Operation(
            operationId = "createToken",
            description = "Create token",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = TokenConfig::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun create(request: ServerRequest): ServerResponse {
        val validator = TokenConfigValidator()

        val authData = try { request.attributes()["authData"] as ServiceAuthenticator.AuthData? } catch(e: Exception) { null } ?: run {
            val error = AppError("Create token fail: AuthData is invalid")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val serviceId = authData.service.getId()
        val roomId = request.pathVariable("roomId")
        val tokenConfig = try { request.awaitBodyOrNull<TokenConfig>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Required arguments must not be null")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(tokenConfig, TokenConfig::class.java.name)
        validator.validate(tokenConfig, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        return try {
            val user = authData.user?: tokenConfig.user
            val role = authData.role?: tokenConfig.role
            val origin = tokenConfig.preference
            val result = tokenService.create(serviceId, roomId, user, role, origin)
            //legacy 호환을 위해 TokenInfo를 사용하지 않는다
            ok().contentType(MediaType.TEXT_PLAIN).bodyValueAndAwait(result)
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Create token fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: IllegalArgumentException) {
            val message = e.message?: ""
            val error = NotFoundError(message)
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: Exception) {
            val message = e.message?: ""
            val error = BadRequestError(message)
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }
}