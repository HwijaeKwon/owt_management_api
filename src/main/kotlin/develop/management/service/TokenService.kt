package develop.management.service

import develop.management.domain.document.Token
import develop.management.repository.KeyRepository
import develop.management.repository.RoomRepository
import develop.management.repository.TokenRepository
import develop.management.rpc.RpcService
import develop.management.util.cipher.Cipher
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.util.*
import kotlin.random.Random

/**
 * token 관련 비즈니스 로직을 담당하는 service class
 */
@Service
class TokenService(private val tokenRepository: TokenRepository,
                   private val keyRepository: KeyRepository,
                   private val roomRepository: RoomRepository,
                   private val rpcService: RpcService) {

    /**
     * token과 key를 조합하여 string형태의 token을 생성한다
     * @return : Base64 encoded string
     */
    suspend fun tokenToString(token: Token): String {
        val key = keyRepository.findById(0)?.getKey() ?: throw IllegalStateException("Key does not exist")
        val message = token.getId() + "," + token.getHost()
        val hash = Cipher.createHmac(message, key, "HmacSHA256")
        val tokenJson: JSONObject = JSONObject().also {
            it.put("tokenId", token.getId())
            it.put("host", token.getHost())
            it.put("secure", token.getSecure())
            it.put("signature", hash)
        }

        return Base64.getEncoder().encodeToString(tokenJson.toString().encodeToByteArray())
    }
    /**
     * 특정 service의 특정 room을 위한 token을 생성한다
     */
    suspend fun create(serviceId: String, roomId: String, user: String, role: String, origin: Token.Origin): String {

        if(user.isBlank()) throw IllegalArgumentException("Name or role not valid")
        val room = roomRepository.findById(roomId)?: throw IllegalArgumentException("Room not found")
        if(room.getRoles().none { it.role == role }) throw Exception("Role is not valid")

        val tokenCode = Random.nextLong(0, 100000000000).toString() + ""

        val (status, result) = rpcService.schedulePortal(tokenCode, origin)

        if(status == "error") throw IllegalStateException("Schedule portal fail. $result")

        val jsonResult = JSONObject(result)
        val secure = if(jsonResult.isNull("under_https_proxy")
                || jsonResult.getBoolean("under_https_proxy")) true else jsonResult.getBoolean("ssl")

        val hostname = jsonResult.getString("hostname")
        val port = jsonResult.getString("port")
        val ip = jsonResult.getString("ip")
        val host = if(hostname.isNotBlank()) "$hostname:$port" else "$ip:$port"

        val savedToken = tokenRepository.save(Token(roomId, serviceId, user, role, origin, tokenCode, secure, host))

        return tokenToString(savedToken)
    }
}