package develop.management.handler

import develop.management.domain.dto.*
import develop.management.service.StreamService
import develop.management.util.error.AppError
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorFoam
import develop.management.util.error.NotFoundError
import develop.management.validator.StreamUpdateValidator
import develop.management.validator.StreamingInRequestValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok
import java.lang.IllegalArgumentException

/**
 * Stream 관련된 요청을 처리하는 handler function 모음
 * Streams model : https://software.intel.com/sites/products/documentation/webrtc/restapi/의 streams 참고 (자사 documentation 필요함)
 */
@Component
class StreamHandler(private val streamService: StreamService) {
    private final val logger = LoggerFactory.getLogger(this.javaClass.name)

    /**
     * 특정 room에 속한 특정 stream을 반환한다
     */
    @Operation(
            operationId = "findOneStream",
            description = "find one stream",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "streamId", description = "Stream id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamInfo::class))]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findOne(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val streamId = request.pathVariable("streamId")
        return try {
            val result = streamService.findOne(roomId, streamId)
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(result)
        } catch (e: IllegalArgumentException) {
            val error = NotFoundError("Stream not found")
            logger.info("Find one stream fail. Not found")
            println("Find one stream fail. Not found")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Fail to find one stream. Reason : $message")
            logger.info("Fail to find one stream. Reason : $message")
            println("Fail to find one stream. Reason : $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room의 모든 stream을 반환한다
     */
    @Operation(
            operationId = "findAllStream",
            description = "find all streams",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = StreamInfo::class)))]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findAll(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        return try {
            val streamList: List<StreamInfo> = streamService.findAll(roomId)
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(streamList)
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Fail to find all streams. Reason : $message")
            logger.info("Fail to find all streams. Reason : $message")
            println("Fail to find all streams. Reason : $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room에 속한 특정 stream을 updateInfo를 반영하여 갱신한다
     */
    @Operation(
            operationId = "updateStream",
            description = "Update stream",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "streamId", description = "Stream id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamUpdate::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamInfo::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleStream)])]),
            ]
    )
    suspend fun update(request: ServerRequest): ServerResponse {
        val validator = StreamUpdateValidator()

        val roomId = request.pathVariable("roomId")
        val streamId = request.pathVariable("streamId")

        val streamUpdate = try { request.awaitBodyOrNull<StreamUpdate>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            logger.info("Fail to update stream. Invalid request body: Request body is not valid.")
            println("Fail to update stream. Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(streamUpdate, StreamUpdate::class.java.name)
        validator.validate(streamUpdate, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            logger.info("Fail to update stream. Invalid request body: $message")
            println("Fail to update stream. Invalid request body: $message")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        return try {
            val result = streamService.update(roomId, streamId, streamUpdate)
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(result)
        } catch (e: IllegalStateException) {
            val error = NotFoundError("Stream not found")
            logger.info("Fail to update stream. Not found")
            println("Fail to update stream. Not found")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room에 속한 특정 stream을 제거한다
     */
    @Operation(
            operationId = "deleteStream",
            description = "Delete stream",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "streamId", description = "Stream id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class), examples = [ExampleObject(value = "Stream deleted")])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun delete(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val streamId = request.pathVariable("streamId")
        return try {
            streamService.delete(roomId, streamId)
            ok().bodyValueAndAwait("Stream deleted")
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Fail to delete streams. Reason : $message")
            logger.info("Fail to delete stream. Reason : $message")
            println("Fail to delete stream. Reason : $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 새로운 streaming in을 추가한다
     */
    @Operation(
            operationId = "addStreamingIn",
            description = "Add streaming in",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            responses = [
            ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = StreamInfo::class))]),
            ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
            ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
        ]
    )
    suspend fun addStreamingIn(request: ServerRequest): ServerResponse {
        val validator = StreamingInRequestValidator()

        val roomId = request.pathVariable("roomId")

        val streamingInRequest = try { request.awaitBodyOrNull<StreamingInRequest>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            logger.info("Fail to add streaming in. Invalid request body: Request body is not valid.")
            println("Fail to add streaming in. Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(streamingInRequest, StreamingInRequest::class.java.name)
        validator.validate(streamingInRequest, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            logger.info("Fail to add streaming in. Invalid request body: $message")
            println("Fail to add streaming in. Invalid request body: $message")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        streamingInRequest.type = "streaming"

        return try {
            val result = streamService.addStreamingIn(roomId, streamingInRequest)
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(result)
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Fail to add streaming in. Reason : $message")
            logger.info("Fail to add streaming in. Reason : $message")
            println("Fail to add streaming in. Reason : $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }
}