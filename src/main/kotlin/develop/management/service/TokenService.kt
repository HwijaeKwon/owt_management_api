package develop.management.service

import com.google.gson.Gson
import develop.management.domain.document.Token
import develop.management.domain.dto.TokenConfig
import develop.management.repository.KeyRepository
import develop.management.repository.RoomRepository
import develop.management.repository.TokenRepository
import develop.management.repository.mongo.RetryOperation
import develop.management.rpc.MessageService
import develop.management.util.cipher.Cipher
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.json.JSONObject
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.*
import kotlin.random.Random

/**
 * token 관련 비즈니스 로직을 담당하는 service class
 */
@Service
class TokenService(private val tokenRepository: TokenRepository,
                   private val keyRepository: KeyRepository,
                   private val roomRepository: RoomRepository,
                   private val messageService: MessageService,
                   private val transactionalOperator: TransactionalOperator,
                   private val retryOperation: RetryOperation
) {

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
    suspend fun create(serviceId: String, roomId: String, tokenConfig: TokenConfig): String {
        val token = Token.createToken(tokenConfig)

        /*val savedToken = retryOperation.execute {
            transactionalOperator.executeAndAwait {
                val room = roomRepository.findById(roomId)?: throw IllegalArgumentException("Room not found")
                if(room.getRoles().none { it.role == token.getRole() }) throw Exception("Role is not valid")
                val code = Random.nextLong(0, 100000000000).toString() + ""
                //Todo: rpc를 통해 portal에 host를 달라고 요청해야한다
                val secure = true
                val host = ""
                token.updateToken(room.getId(), serviceId, code, secure, host)
                //token repository error를 고려해야 한다 -> token은 null이 될 수 없다
                tokenRepository.save(token)
            }!!
        }*/

        val room = roomRepository.findById(roomId)?: throw IllegalArgumentException("Room not found")
        if(room.getRoles().none { it.role == token.getRole() }) throw Exception("Role is not valid")
        val code = Random.nextLong(0, 100000000000).toString() + ""
        //Todo: rpc를 통해 portal에 host를 달라고 요청해야한다
        val message = Gson().toJson(TestMessage("test message 2"))
        val reply_stream = messageService.sendMessage(message)
        val reply_str = reply_stream.toMono()
            .awaitSingleOrNull()
        println("reply str !!!!!!! : $reply_str")
        val secure = true
        val host = ""
        token.updateToken(room.getId(), serviceId, code, secure, host)
        //token repository error를 고려해야 한다 -> token은 null이 될 수 없다
        val savedToken = tokenRepository.save(token)

        return tokenToString(savedToken)
    }

    data class TestMessage(val message: String)
}