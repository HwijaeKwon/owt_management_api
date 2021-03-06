package develop.management.rpc

import org.json.JSONObject
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import kotlin.random.Random

@Service
class RpcService {

    private val maxCorrId = 100000L

    // 메세지 수신 sink를 관리하는 map
    private val processorMap: HashMap<Long, Sinks.Many<String>> = hashMapOf()

    // 메세지 전송 시 사용하는 sink
    val sendProcessor = Sinks.many().unicast().onBackpressureBuffer<Message<String>>()

    private fun generateCorrId(): Long {
        val candidate = Random.nextLong(maxCorrId)
        processorMap[candidate]?.run { throw IllegalStateException("Generate Corr Id fail") }
        return candidate
    }

    suspend fun sendMessage(routingKey: String, message: String): Flux<String> {
        val corrId = Random.nextLong(maxCorrId)
        processorMap[corrId]?.run { throw IllegalStateException("Generate Corr Id fail") }
            ?: run { processorMap[corrId] = Sinks.many().unicast().onBackpressureBuffer() }

        val jsonMessage = JSONObject()
        jsonMessage.put("message", message)
        jsonMessage.put("corrId", corrId)

        val msg = MessageBuilder.withPayload(jsonMessage.toString())
            .setHeader("routingKey", routingKey)
            .build()

        return processorMap[corrId]!!.asFlux().apply { sendProcessor.emitNext(msg, Sinks.EmitFailureHandler.FAIL_FAST) }
    }

    suspend fun receiveMessage(message: String) {
        val jsonMessage = JSONObject(message)
        val corrId = jsonMessage.getLong("corrId")
        processorMap[corrId]?.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST)
            ?: throw IllegalStateException("Receiver processor not found")
        processorMap.remove(corrId)
    }
}