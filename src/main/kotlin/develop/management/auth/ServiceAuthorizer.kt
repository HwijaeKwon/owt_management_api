package develop.management.auth

import develop.management.ManagementInitializer
import develop.management.util.error.AppError
import develop.management.util.error.AuthorizationError
import org.springframework.context.annotation.DependsOn
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * client의 service의 권한을 확인하는 클래스
 * 요청에 대한 권한이 있으면 다른 filter 혹은 handler function으로 요청을 전달한다
 * 요청에 대한 권한이 없으면 다른 client에게 access error 메세지를 전달한다
 */

@Component
@DependsOn(value = ["managementInitializer"])
class ServiceAuthorizer(private val initializer: ManagementInitializer) {

    /**
     * service를 대상으로 한 요청일 경우 이에 대한 권한을 확인한다
     */
    suspend fun serviceAuthorize(request: ServerRequest, next: suspend (ServerRequest) -> (ServerResponse)): ServerResponse {
        val authData = try { request.attributes()["authData"] as ServiceAuthenticator.AuthData? } catch(e: Exception) { null } ?: run {
            val error = AppError("ServiceAuthorizer serviceAuthorize fail: AuthData is invalid")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val superServiceId = initializer.getSuperServiceId()

        val authServiceId = authData.service.getId()

        val serviceId = request.pathVariables()["serviceId"]

        //superService 관련 요청
        if(serviceId == null && (superServiceId == authServiceId)) return next(request)

        if((superServiceId == authServiceId) || (authServiceId == serviceId)) {
            if(request.method() == HttpMethod.DELETE && serviceId == superServiceId) {
                val error = AuthorizationError("Permission denied: Super service deletion is not permitted")
                return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
            }
            return next(request)
        }

        val error = AuthorizationError("Permission denied")
        return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
    }
}