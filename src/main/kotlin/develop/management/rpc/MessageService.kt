package develop.management.rpc

import com.google.gson.Gson
import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.function.Consumer
import java.util.function.Supplier

@Service
class MessageService {

    private val unicastProcessor = Sinks.many().unicast().onBackpressureBuffer<Message<String>>()

    private val unicastProcessor2 = Sinks.many().unicast().onBackpressureBuffer<String>()

    private val processorMap: MutableMap<Long, Sinks.Many<String>> = mutableMapOf()

    fun getProducer() = unicastProcessor

    suspend fun sendMessage(message: String): Flux<String> {
        val msg = MessageBuilder.withPayload(message)
            .setHeader("routeTo", "test-receiver")
            .build()
        //random한 값을 만드는 게 더 좋을 것 같다
        processorMap[0L] = unicastProcessor2
        return unicastProcessor2.asFlux().apply { unicastProcessor.emitNext(msg, Sinks.EmitFailureHandler.FAIL_FAST) }
    }

    private suspend fun processMessage(message: String): String? {
        println("rx!!! : $message")
        processorMap[0L]?.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST)
        return null
    }

    @Bean
    fun tx(): Supplier<Flux<Message<String>>> = Supplier {
        this.getProducer().asFlux()
    }

    @Bean
    fun rx(): Consumer<Flux<Message<String>>> = Consumer { stream ->
        stream.concatMap { msg ->
            mono { processMessage(msg.payload) }
        }.onErrorContinue { e, _ ->
            println(e.message)
        }.subscribe()
    }
}