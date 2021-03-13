package develop.management.auth

import develop.management.domain.document.Service
import develop.management.repository.ServiceRepository
import develop.management.util.cipher.Cipher
import develop.management.util.error.AuthenticationError
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.util.*

/**
 * client의 service를 인증하는 클래스
 * 인증에 성공하면 요청에 있던 service(필요에 따라 user와 role까지)를 db에서 가져오고
 * 다른 filter 혹은 handler function에게 요청을 전달한다
 * service(필요에 따라 user와 role)은 이후에 비즈니스 로직을 처리할 때 사용할 수 있다
 * 인증에 실패하면 client에게 인증 실패를 알려준다
 */

@Component
class ServiceAuthenticator(private val serviceRepository: ServiceRepository) {
    private final val logger = LoggerFactory.getLogger(this.javaClass.name)

    /**
     * 인증된 데이터를 handler function에게 전달하기 위해 사용하는 클래스
     * Domain객체라기 보다는 DTO에 가까워서 우선은 domain package에 넣지 않고 ServerAuthenticator 내부에 두었다
     */
    data class AuthData (val service: Service, val user: String? = null, val role: String? = null)

    suspend fun authenticate(request: ServerRequest, next: suspend (ServerRequest) -> (ServerResponse)): ServerResponse {
        //Error 발생시 client에게 보낼 메세지 내용
        val challengeReq = "MAuth realm=\"http://marte3.dit.upm.es\""
        val error = AuthenticationError("WWW-Authenticate: $challengeReq")

        //Authorization header가 없을 경우 client에게 error 메세지를 보낸다
        val authHeader = request.headers().header("Authorization").firstOrNull()
                ?: run {
                    logger.info("Authorization header not found")
                    return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody) }

        val parsedAuthHeader = parseAuthHeader(authHeader)

        //Authorization header 안에 serviceId가 없을 경우 client에게 error 메세지를 보낸다
        val serviceId = parsedAuthHeader["serviceid"]
                ?: run {
                    logger.info("Service id not found in authorization header")
                    return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody) }

        //service가 존재하지 않을 경우 client에게 error 메세지를 보낸다
        //service id는 null이 될 수 없다
        val serviceData = serviceRepository.findById(serviceId)
                ?: run {
                    logger.info("Service not found")
                    return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody) }

        var key = serviceData.getKey()
        key = if(serviceData.getEncrypted()) Cipher.decrypt(key) else key

        if(!checkSignature(parsedAuthHeader, key)) {
            logger.info("CheckSignature fail")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        //Todo: client가 user와 role을 보낼 것인지, 보낸다면 어떤 방식으로 인코딩해둘 것인지 결정해야 한다
        request.attributes()["authData"] = AuthData(serviceData, parsedAuthHeader["user"], parsedAuthHeader["role"])

        return next(request)
    }

    fun checkSignature(data: Map<String, String>, key: String) : Boolean {

        if(data["signature_method"] != "HMAC_SHA256") return false

        if(data["timestamp"] == null || data["cnonce"] == null) return false

        var message = data["timestamp"] + "," + data["cnonce"]

        if(data["username"] != null && data["role"] != null)
            message += "," + data["username"] + "," + data["role"]

        val signature = Cipher.createHmac(message, key, "HmacSHA256")

        //Todo: 디버깅 중
        logger.debug("Created signature : $signature")
        logger.debug("Received signature : " + data["signature"])
        return (signature == data["signature"])
    }

    /**
     * Authorization header를 parsing한다
     * Authorization header는 아래와 같이 구성되어 있다
     * MAuth realm=http://marte3.dit.upm.es,
     * mauth_signature_method=HMAC_SHA256,
     * mauth_serviceid=serviceid,
     * mauth_cnonce=cnonce,
     * mauth_timestamp=timestamp,
     * mauth_signature=signature
     */
    private fun parseAuthHeader(authHeader: String): Map<String, String> {
        val params: MutableMap<String, String> = mutableMapOf()

        authHeader.split(",")
                .forEach {
                    var value = ""
                    val item = it.split("=")
                    item.forEachIndexed { index, contents ->
                        if(index == 0) return@forEachIndexed
                        value += if(contents.isBlank()) "=" else contents
                    }
                    params[item[0].substring(6)] = value
                }

        return params
    }
}