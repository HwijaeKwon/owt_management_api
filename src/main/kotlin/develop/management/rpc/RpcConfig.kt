package develop.management.rpc

import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import reactor.core.publisher.Flux
import java.util.function.Consumer
import java.util.function.Supplier

@Configuration
@ComponentScan(basePackages = ["develop.management.rpc"])
class RpcConfig {

    @Autowired
    private lateinit var rpcService: RpcService

    @Bean
    fun tx(): Supplier<Flux<Message<String>>> = Supplier {
        rpcService.sendProcessor.asFlux()
    }

    @Bean
    fun rx(): Consumer<Flux<Message<String>>> = Consumer { stream ->
        stream.concatMap { msg ->
            mono { rpcService.receiveMessage(msg.payload) }
        }.onErrorContinue { e, _ ->
            println(e.message)
        }.subscribe()
    }
}