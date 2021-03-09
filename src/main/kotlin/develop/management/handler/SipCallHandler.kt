package develop.management.handler

import develop.management.domain.dto.*
import develop.management.service.RecordingService
import develop.management.service.SipCallService
import develop.management.service.StreamingOutService
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
 * SipCall 관련된 요청을 처리하는 handler function 모음
 */
@Component
class SipCallHandler(private val sipCallService: SipCallService) {

    /**
     * 모든 sipcall을 조회한다
     */
    @Operation(
            operationId = "findAllSipCalls",
            description = "find all sipcalls",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = SipCall::class)))]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findAll(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")

        return try {
            val sipCalls = sipCallService.findAll(roomId)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(sipCalls)
        } catch (e: IllegalStateException) {
            val message = e.message ?: "Rpc error"
            val error = AppError("Find all sipcalls fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 새로운 sipcall을 추가한다
     */
    @Operation(
            operationId = "add",
            description = "Add sipcall",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = SipCallRequest::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = SipCall::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun add(request: ServerRequest): ServerResponse {
        val validator = SipCallRequestValidator()

        val sipCallRequest = try { request.awaitBodyOrNull<SipCallRequest>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(sipCallRequest, SipCallRequest::class.java.name)
        validator.validate(sipCallRequest, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val roomId = request.pathVariable("roomId")

        return try {
            val sipCall = sipCallService.create(roomId, sipCallRequest)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(sipCall)
        } catch (e: IllegalStateException) {
            val message = e.message?: "Rpc error"
            val error = AppError("Add sipcall fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * SipCall을 업데이트 한다
     */
    @Operation(
            operationId = "updateSipCall",
            description = "Update sipcall",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "sipCallId", description = "SipCall id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = MediaOutControlInfo::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = SipCall::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun update(request: ServerRequest): ServerResponse {
        val validator = MediaOutControlInfoValidator()

        val mediaOutControlInfo = try { request.awaitBodyOrNull<MediaOutControlInfo>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(mediaOutControlInfo, MediaOutControlInfo::class.java.name)
        validator.validate(mediaOutControlInfo, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val roomId = request.pathVariable("roomId")
        val sipCallId = request.pathVariable("sipCallId")

        return try {
            val sipCall = sipCallService.update(roomId, sipCallId, mediaOutControlInfo)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(sipCall)
        } catch (e: IllegalStateException) {
            val message = e.message ?: "Rpc error"
            val error = AppError("Update sipcall fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 sipcall을 제거한다
     */
    @Operation(
            operationId = "deleteSipCall",
            description = "Delete sipcall",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "sipCallId", description = "SipCall Id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class), examples = [ExampleObject(value = "SipCall deleted")])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun delete(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val sipCallId = request.pathVariable("sipCallId")
        return try {
            sipCallService.delete(roomId, sipCallId)
            ServerResponse.ok().bodyValueAndAwait("SipCall deleted")
        } catch (e: IllegalStateException) {
            val message = e.message ?: ""
            val error = AppError("Delete sipcall failed: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }
}