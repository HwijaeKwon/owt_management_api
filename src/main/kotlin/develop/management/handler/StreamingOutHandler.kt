package develop.management.handler

import develop.management.domain.dto.*
import develop.management.service.StreamingOutService
import develop.management.util.error.AppError
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorFoam
import develop.management.validator.StreamingOutRequestValidator
import develop.management.validator.SubscriptionControlInfoValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kotlinx.coroutines.flow.toList
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.*

/**
 * Streaming out 관련된 요청을 처리하는 handler function 모음
 */
@Component
class StreamingOutHandler(private val streamingOutService: StreamingOutService) {

    /**
     * 모든 streaming out을 조회한다
     */
    @Operation(
            operationId = "findAllStreamingOuts",
            description = "find all streamingouts",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = StreamingOut::class)))]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findAll(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")

        return try {
            val streamingOut = streamingOutService.findAll(roomId)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(streamingOut)
        } catch (e: IllegalStateException) {
            val message = e.message ?: "Rpc error"
            val error = AppError("Find all streaming out fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 새로운 streaming out을 추가한다
     */
    @Operation(
            operationId = "add",
            description = "Add streaming out",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamingOutRequest::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamingOut::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun add(request: ServerRequest): ServerResponse {
        val validator = StreamingOutRequestValidator()

        val streamingOutRequest = try { request.awaitBodyOrNull<StreamingOutRequest>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(streamingOutRequest, PermissionUpdate::class.java.name)
        validator.validate(streamingOutRequest, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val roomId = request.pathVariable("roomId")

        return try {
            val streamingOut = streamingOutService.create(roomId, streamingOutRequest)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(streamingOut)
        } catch (e: IllegalStateException) {
            val message = e.message?: "Rpc error"
            val error = AppError("Update participant fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * Streaming out을 업데이트 한다
     */
    @Operation(
            operationId = "updateStreamingOut",
            description = "Update streaming out",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "streamingOutId", description = "Streaming out id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = SubscriptionControlInfo::class, required = true)))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamingOut::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun update(request: ServerRequest): ServerResponse {
        val validator = SubscriptionControlInfoValidator()

        val subscriptionControlInfoList = try { request.bodyToFlow<SubscriptionControlInfo>().toList() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        subscriptionControlInfoList.forEach {
            val errors = BeanPropertyBindingResult(it, SubscriptionControlInfo::class.java.name)
            validator.validate(it, errors)
            if(errors.allErrors.isNotEmpty()) {
                var message = "Invalid request body: "
                errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
                val error = BadRequestError(message)
                return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }
        }

        val roomId = request.pathVariable("roomId")
        val streamingOutId = request.pathVariable("streamingOutId")

        return try {
            val streamingOut = streamingOutService.update(roomId, streamingOutId, subscriptionControlInfoList)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(streamingOut)
        } catch (e: IllegalStateException) {
            val message = e.message ?: "Rpc error"
            val error = AppError("Update participant fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 streaming out을 제거한다
     */
    @Operation(
            operationId = "deleteStreamingOut",
            description = "Delete streaming out",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "streamingOutId", description = "Streaming out id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class), examples = [ExampleObject(value = "StreamingOut deleted")])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun delete(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val streamingOutId = request.pathVariable("streamingOutId")
        return try {
            streamingOutService.delete(roomId, streamingOutId)
            ServerResponse.ok().bodyValueAndAwait("StreamingOut deleted")
        } catch (e: IllegalStateException) {
            val message = e.message ?: ""
            val error = AppError("Delete streaming out failed: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }
}