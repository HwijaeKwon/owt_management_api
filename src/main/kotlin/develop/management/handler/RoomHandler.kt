package develop.management.handler

import develop.management.auth.ServiceAuthenticator
import develop.management.domain.document.Room
import develop.management.domain.dto.*
import develop.management.service.RoomService
import develop.management.util.error.AppError
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorFoam
import develop.management.util.error.NotFoundError
import develop.management.validator.RoomConfigValidator
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
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * 방 관련된 요청을 처리하는 handler function 모음
 * Rooms model : https://software.intel.com/sites/products/documentation/webrtc/restapi/의 rooms 참고 (자사 documentation 필요함)
 */
@Component
class RoomHandler(private val roomService: RoomService) {

    /**
     * 새로운 room을 db에 생성한다
     */
    @Operation(
            operationId = "createRoom",
            description = "Create room",
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = RoomConfig::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = RoomInfo::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun create(request: ServerRequest): ServerResponse {
        val validator = RoomConfigValidator()

        val authData = try { request.attributes()["authData"] as ServiceAuthenticator.AuthData? } catch(e: Exception) { null } ?: run {
            val error = AppError("Create room fail: AuthData is invalid")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val serviceId = authData.service.getId()

        val roomConfig = try { request.awaitBodyOrNull<RoomConfig>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(roomConfig, RoomConfig::class.java.name)
        validator.validate(roomConfig, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        return try {
            val room = Room.create(roomConfig)
            val result = roomService.create(serviceId, room)
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(RoomInfo.create(result))
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Create room failed: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room을 반환한다
     */
    @Operation(
            operationId = "findOneRoom",
            description = "find one room",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = RoomInfo::class))]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findOne(request: ServerRequest): ServerResponse {
        val authData = try { request.attributes()["authData"] as ServiceAuthenticator.AuthData? } catch(e: Exception) { null } ?: run {
            val error = AppError("Create room fail: AuthData is invalid")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val serviceId = authData.service.getId()

        val roomId = request.pathVariable("roomId")

        return try {
            roomService.findOne(serviceId, roomId)?.let {
                ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(RoomInfo.create(it))
            }?: throw IllegalArgumentException("Room not found")
        } catch (e: IllegalArgumentException) {
            val message = e.message?: "Not found error"
            val error = NotFoundError(message)
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 모든 room을 반환한다
     */
    @Operation(
            operationId = "findAllRoom",
            description = "find all rooms",
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = RoomInfo::class)))]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun findAll(request: ServerRequest): ServerResponse {
        val authData = try { request.attributes()["authData"] as ServiceAuthenticator.AuthData? } catch(e: Exception) { null } ?: run {
            val error = AppError("Find all rooms fail: AuthData is invalid")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val serviceId = authData.service.getId()

        return try {
            val data = roomService.findAll(serviceId)
            //legacy 호환을 위해 RoomInfos를 사용하지 않는다
            val result = data.map { RoomInfo.create(it) }
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(result)
        } catch (e: IllegalArgumentException) {
            val message = e.message?: "Not found error"
            val error = NotFoundError(message)
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room의 정보를 갱신한다
     */
    @Operation(
            operationId = "updateRoom",
            description = "Update room",
            parameters = [Parameter(name = "roomId", description = "Room id", required = true)],
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = UpdateOptions::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = RoomInfo::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun update(request: ServerRequest): ServerResponse {
        val authData = try { request.attributes()["authData"] as ServiceAuthenticator.AuthData? } catch(e: Exception) { null } ?: run {
            val error = AppError("Find all rooms fail: AuthData is invalid")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val serviceId = authData.service.getId()

        val roomId = request.pathVariable("roomId")

        val update = try { request.awaitBodyOrNull<UpdateOptions>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Request body is not valid.")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        return try {
            roomService.update(serviceId, roomId, update)?.let {
                ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(RoomInfo.create(it))
            }?: throw IllegalArgumentException("Room not found")
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Update room failed: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: IllegalArgumentException) {
            val message = e.message?: ""
            val error = NotFoundError(message)
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 room을 제거한다
     */
    @Operation(
            operationId = "deleteRoom",
            description = "Delete room",
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class), examples = [ExampleObject(value = "Room deleted")])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun delete(request: ServerRequest): ServerResponse {
        val authData = try { request.attributes()["authData"] as ServiceAuthenticator.AuthData? } catch(e: Exception) { null } ?: run {
            val error = AppError("Find all rooms fail: AuthData is invalid")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val serviceId = authData.service.getId()
        val roomId = request.pathVariable("roomId")
        return try {
            roomService.delete(serviceId, roomId)
            ok().bodyValueAndAwait("Room deleted")
        } catch (e: IllegalArgumentException) {
            val message = e.message?: ""
            val error = NotFoundError(message)
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }
}