package develop.management.handler

import develop.management.domain.dto.*
import develop.management.service.AnalyticsService
import develop.management.service.SipCallService
import develop.management.util.error.AppError
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorFoam
import develop.management.validator.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * Analytics 관련된 요청을 처리하는 handler function 모음
 */
@Component
class AnalyticsHandler(private val analyticsService: AnalyticsService) {

    /**
     * 모든 sipcall을 조회한다
     */
    @Operation(
            operationId = "findAllAnalytics",
            description = "find all analytics",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = Analytics::class)))]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findAll(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")

        return try {
            val analytics = analyticsService.findAll(roomId)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(analytics)
        } catch (e: IllegalStateException) {
            val message = e.message ?: "Rpc error"
            val error = AppError("Find all analytics fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 새로운 analytics를 추가한다
     */
    @Operation(
            operationId = "add",
            description = "Add analytics",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = AnalyticsRequest::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = Analytics::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun add(request: ServerRequest): ServerResponse {
        val validator = AnalyticsRequestValidator()

        val analyticsRequest = try { request.awaitBodyOrNull<AnalyticsRequest>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(analyticsRequest, AnalyticsRequest::class.java.name)
        validator.validate(analyticsRequest, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val roomId = request.pathVariable("roomId")

        return try {
            val analytics = analyticsService.create(roomId, analyticsRequest)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(analytics)
        } catch (e: IllegalStateException) {
            val message = e.message?: "Rpc error"
            val error = AppError("Add analytics fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 analytics를 제거한다
     */
    @Operation(
            operationId = "deleteAnalytics",
            description = "Delete analytics",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "analyticsId", description = "Analytics Id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class), examples = [ExampleObject(value = "Analytics deleted")])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun delete(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val analyticsId = request.pathVariable("analyticsId")
        return try {
            analyticsService.delete(roomId, analyticsId)
            ServerResponse.ok().bodyValueAndAwait("Analytics deleted")
        } catch (e: IllegalStateException) {
            val message = e.message ?: ""
            val error = AppError("Delete analytics failed: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }
}