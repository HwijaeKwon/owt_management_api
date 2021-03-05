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

    private val unicastProcessor = Sinks.many().unicast().onBackpressureBuffer<String>()

    fun getProducer() = unicastProcessor

    suspend fun sendMessage(message: String) {
        //val msg = MessageBuilder.withPayload(message).build()
        unicastProcessor.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST)
    }

    @Bean
    fun tx(): Supplier<Flux<String>> = Supplier {
        this.getProducer().asFlux()
    }
}