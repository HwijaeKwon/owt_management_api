package develop.management.rpc

import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function

@Service
class MessageConsumerService {

    @Bean
    fun rx(): Function<Flux<String>, Mono<Void>> = Function { stream ->
        stream.doOnNext{message -> println("rx !!! $message")}.then()
    }
}