package develop.management.rpc

import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Consumer
import java.util.function.Function

@Service
class MessageConsumerService {

    fun processMessage(message: String): String? {
        println("rx!!! : $message")
        return null
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