package develop.management.rpc

import com.google.gson.Gson
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.function.Consumer

@Service
class MessageConsumerService {

    @Autowired
    private lateinit var messageProducerService: MessageProducerService

    suspend fun processMessage(message: String): String? {
        println("rx!!! : $message")
        val messageJson = Gson().toJson(TestMessage("test message from Spring"))
        messageProducerService.sendMessage(messageJson)
        return null
    }

    data class TestMessage(val message: String)

   /* @Bean
    fun rx(): Consumer<Flux<Message<String>>> = Consumer { stream ->
        stream.concatMap { msg ->
            mono { processMessage(msg.payload) }
        }.onErrorContinue { e, _ ->
            println(e.message)
        }.subscribe()
    }*/
}