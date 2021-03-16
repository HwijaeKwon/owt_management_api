package develop.management.rpc

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.lang.Thread.sleep

@ActiveProfiles("test")
@Import(RabbitMqConfig::class)
class RpcControllerTest {

    @Autowired
    private lateinit var rabbitMq: EmbeddedRabbitMq

    @BeforeEach
    fun init() {
        rabbitMq.start()
    }

    @AfterEach
    fun close() {
        rabbitMq.stop()
    }

    @Test
    fun test() {
        sleep(1000)
    }
}