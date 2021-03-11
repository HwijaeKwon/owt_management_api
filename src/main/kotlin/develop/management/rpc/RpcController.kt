package develop.management.rpc

import kotlinx.coroutines.reactor.mono
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.locks.LockSupport
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.collections.HashMap
import kotlin.random.Random

@Component
@PropertySource(value = ["classpath:application.yml"])
class RpcController(private val environment: Environment) {

    private val bindingRoutingKey = environment.getProperty("spring.cloud.stream.rabbit.bindings.rx-in-0.consumer.binding-routing-key", "management")

    // maximum corrID
    private val maxCorrID = environment.getProperty("rpc.maxCorrID", Long::class.java, 10000)

    // 메세지 수신 sink를 관리하는 map
    private val processorMap: HashMap<Long, Sinks.Many<Pair<String, String>>> = hashMapOf()

    // 메세지 전송 시 사용하는 sink
    private val sendProcessor = Sinks.many().unicast().onBackpressureBuffer<Message<String>>()

    fun deleteCorrID(corrID: Long) {
        processorMap.remove(corrID)
    }

    suspend fun sendMessage(routingKey: String, method: String, args: JSONArray, corrId: Long? = null): Pair<Flux<Pair<String, String>>, Long> {
        val corrID = corrId?: let {
            val candidate = Random.nextLong(maxCorrID)
            processorMap[candidate]?.run { throw IllegalStateException("Generate Corr ID fail") }
                ?: run { processorMap[candidate] = Sinks.many().unicast().onBackpressureBuffer() }
            return@let candidate
        }

        val message = JSONObject()
        message.put("method", method)
        message.put("args", args)
        message.put("corrID", corrID)
        message.put("replyTo", bindingRoutingKey)
        val msg = MessageBuilder.withPayload(message.toString())
            .setHeader("routingKey", routingKey)
            .build()

        return Pair(processorMap[corrID]!!.asFlux(), corrID)
                .apply { sendProcessor.emitNext(msg) { signalType, emitResult ->
                    if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                        LockSupport.parkNanos(10)
                        true
                    } else false
                }
            }
    }

    private suspend fun receiveMessage(message: String) {
        val jsonMessage = JSONObject(message)
        val corrID = jsonMessage.getLong("corrID")
        val data = jsonMessage.getString("data")
        val err = try { jsonMessage.getString("err") } catch (e: JSONException) { "" }
        processorMap[corrID]?.emitNext(Pair(data, err), Sinks.EmitFailureHandler.FAIL_FAST)
            ?: throw IllegalStateException("Receiver processor not found: $corrID")
    }

    @Bean
    fun tx(): Supplier<Flux<Message<String>>> = Supplier {
        this.sendProcessor.asFlux()
    }

    @Bean
    fun rx(): Consumer<Flux<Message<String>>> = Consumer { stream ->
        stream.concatMap { msg ->
            mono { receiveMessage(msg.payload) }
        }.onErrorContinue { e, _ ->
            println(e.message)
        }.subscribe()
    }
}