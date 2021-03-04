package develop.management.handler

import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.StreamInfo
import develop.management.domain.dto.StreamUpdate
import develop.management.domain.dto.UpdateOptions
import develop.management.service.StreamService
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorFoam
import develop.management.util.error.NotFoundError
import develop.management.validator.StreamUpdateValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.json.JSONObject
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok

/**
 * Stream 관련된 요청을 처리하는 handler function 모음
 * Streams model : https://software.intel.com/sites/products/documentation/webrtc/restapi/의 streams 참고 (자사 documentation 필요함)
 */
@Component
class StreamHandler(private val streamService: StreamService) {

    //Todo: Stream data도 JSONObejct가 아니라 data class로 관리해야 한다

    /**
     * 특정 room에 속한 특정 stream을 반환한다
     */
    @Operation(
            operationId = "findOneStream",
            description = "find one stream",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "participantId", description = "Participant id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamInfo::class))]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
            ]
    )
    suspend fun findOne(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val streamId = request.pathVariable("streamId")
        //Todo: error exception 처리하기
        return streamService.findOne(roomId, streamId)?.let {
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(it)
        }?: run {
            val error = NotFoundError("Stream not found")
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
            ]
    )
    suspend fun findAll(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val streamList: List<StreamInfo> = streamService.findAll(roomId)
        //Todo: error exception 처리하기
        return ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(streamList)
    }

    /**
     * 특정 room에 속한 특정 stream을 updateInfo를 반영하여 갱신한다
     */
    @Operation(
            operationId = "updateStream",
            description = "Update stream",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "participantId", description = "Participant id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamUpdate::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = StreamInfo::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
            ]
    )
    suspend fun update(request: ServerRequest): ServerResponse {
        val validator = StreamUpdateValidator()

        val roomId = request.pathVariable("roomId")
        val streamId = request.pathVariable("streamId")

        val streamUpdate = try { request.awaitBodyOrNull<StreamUpdate>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(streamUpdate, RoomConfig::class.java.name)
        validator.validate(streamUpdate, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        //Todo: error exception 처리하기
        return streamService.update(roomId, streamId, streamUpdate)?.let {
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(it)
        }?: run {
            val error = NotFoundError("Stream not found")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room에 속한 특정 stream을 제거한다
     */
    @Operation(
            operationId = "deleteStream",
            description = "Delete stream",
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class), examples = [ExampleObject(value = "Stream deleted")])]),
            ]
    )
    suspend fun delete(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val streamId = request.pathVariable("streamId")
        //Todo: error exception 처리하기
        streamService.delete(roomId, streamId)
        return ok().bodyValueAndAwait("Stream deleted")
    }
}