package develop.management.rpc

import org.springframework.beans.factory.annotation.Configurable
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Flux
import java.util.function.Supplier
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Sinks


@Configurable
class Rpc {
    private val directSink = Sinks.many().multicast().onBackpressureBuffer<MyMessage>()

    var directProcessor = directSink.asFlux()

    @Bean
    fun direct(): Supplier<Flux<MyMessage>> {
        directSink.tryEmitNext(MyMessage("test"))
        directProcessor.then()
        return Supplier<Flux<MyMessage>> { this.directProcessor }
    }
}