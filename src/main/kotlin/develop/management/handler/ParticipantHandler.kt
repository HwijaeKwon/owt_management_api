package develop.management.handler

import develop.management.domain.dto.ParticipantDetail
import develop.management.domain.dto.PermissionUpdate
import develop.management.service.ParticipantService
import develop.management.util.error.AppError
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorFoam
import develop.management.util.error.NotFoundError
import develop.management.validator.PermissionUpdateValidator
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
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * 사용자 관련된 요청을 처리하는 handler function 모음
 */
@Component
class ParticipantHandler(private val participantService: ParticipantService) {
    private final val logger = LoggerFactory.getLogger(this.javaClass.name)

    /**
     * 개별 사용자를 조회한다
     */
    @Operation(
            operationId = "findOneParticipant",
            description = "find one participant",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "participantId", description = "Participant id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = ParticipantDetail::class))]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findOne(request: ServerRequest): ServerResponse {
        //해당 room에 participant가 있는지는 participant authorize 과정에서 확인한다
        val roomId = request.pathVariable("roomId")
        val participantId = request.pathVariable("participantId")
        return try {
            val participantDetail = participantService.findOne(roomId, participantId)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(participantDetail)
        } catch (e: IllegalStateException) {
            val message = e.message ?: "Rpc error"
            val error = AppError("Find one participant fail: $message")
            logger.info("Fail one participant fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: IllegalArgumentException) {
            val message = e.message?: ""
            val error = NotFoundError(message)
            logger.info("Not found: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room의 모든 사용자를 조회한다
     */
    @Operation(
            operationId = "findAllParticipant",
            description = "find all participants",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "participantId", description = "Participant id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = ParticipantDetail::class)))]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findAll(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")

        return try {
            val participantDetailList = participantService.findAll(roomId)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(participantDetailList)
        } catch (e: IllegalStateException) {
            val message = e.message?: "Rpc error"
            val error = AppError("Find all participants fail: $message")
            logger.info("Find all participants fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: IllegalArgumentException) {
            val message = e.message?: ""
            val error = NotFoundError(message)
            logger.info("Find all participants fail. Not found: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room의 특정 사용자의 권한 정보를 업데이트 한다
     */
    @Operation(
            operationId = "updateParticipant",
            description = "Update participant",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "participantId", description = "Participant id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = PermissionUpdate::class, required = true)))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = ParticipantDetail::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun update(request: ServerRequest): ServerResponse {
        val validator = PermissionUpdateValidator()

        val permissionUpdateList = try { request.awaitBodyOrNull<List<PermissionUpdate>>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            logger.info("Update participant fail. Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        permissionUpdateList.forEach {
            val errors = BeanPropertyBindingResult(it, PermissionUpdate::class.java.name)
            validator.validate(it, errors)
            if(errors.allErrors.isNotEmpty()) {
                var message = "Invalid request body: "
                errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
                val error = BadRequestError(message)
                logger.info("Update participant fail. Invalid request body: $message")
                return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }
        }

        val roomId = request.pathVariable("roomId")
        val participantId = request.pathVariable("participantId")

        return try {
            val participantDetail = participantService.update(roomId, participantId, permissionUpdateList)
            ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(participantDetail)
        } catch (e: IllegalStateException) {
            val message = e.message?: "Rpc error"
            val error = AppError("Update participant fail: $message")
            logger.info("Update participant fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: IllegalArgumentException) {
            val message = e.message?: ""
            val error = NotFoundError(message)
            logger.info("Update participant fail. Not found: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room의 특정 사용자를 제거한다
     */
    @Operation(
            operationId = "deleteParticipant",
            description = "Delete participant",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true), Parameter(name = "participantId", description = "Participant id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class), examples = [ExampleObject(value = "Participant deleted")])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun delete(request: ServerRequest): ServerResponse {
        val roomId = request.pathVariable("roomId")
        val participantId = request.pathVariable("participantId")
        return try {
            participantService.delete(roomId, participantId)
            ServerResponse.ok().bodyValueAndAwait("Participant deleted")
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Delete participant failed: $message")
            logger.info("Delete participant failed: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: IllegalArgumentException) {
            val message = e.message?: ""
            val error = NotFoundError(message)
            logger.info("Delete participant failed. Not found: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }
}