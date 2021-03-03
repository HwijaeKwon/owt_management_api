package develop.management.validator

import develop.management.auth.ServiceAuthenticator
import develop.management.repository.RoomRepository
import develop.management.util.error.AppError
import develop.management.util.error.NotFoundError
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * client의 요청이 유효한지 확인하는 클래스
 */

@Component("v1RoomValidator")
class RoomValidator(private val roomRepository: RoomRepository) {

    /**
     * 개별 room을 대상으로 한 요청일 경우 해당 room이 존재하는지 확인한다
     */
    suspend fun validate(request: ServerRequest, next: suspend (ServerRequest) -> (ServerResponse)): ServerResponse {
        val roomId = request.pathVariable("roomId")

        val room = roomRepository.findById(roomId)

        if(room == null) {
            val error = NotFoundError("Room not found")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val authData = try { request.attributes()["authData"] as ServiceAuthenticator.AuthData? } catch(e: Exception) { null } ?: run {
            val error = AppError("RoomValidator fail: AuthData is invalid")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val service = authData.service

        if(service.getRooms().none{ it == roomId }) {
            val error = NotFoundError("Room not found")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        return next(request)
    }
}