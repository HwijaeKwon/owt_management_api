package develop.management.rpc

import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.function.Supplier

@Service
class MessageProducerService {

    private val unicastProcessor = Sinks.many().unicast().onBackpressureBuffer<Message<String>>()

    fun getProducer() = unicastProcessor

    suspend fun sendMessage(message: String) {
        val msg = MessageBuilder.withPayload(message)
            .setHeader("routeTo", "test-receiver")
            .build()
        unicastProcessor.emitNext(msg, Sinks.EmitFailureHandler.FAIL_FAST)
    }

    @Bean
    fun tx(): Supplier<Flux<Message<String>>> = Supplier {
        this.getProducer().asFlux()
    }
}