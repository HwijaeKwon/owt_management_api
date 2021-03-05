package develop.management

import develop.management.rpc.MessageConsumerService
import develop.management.rpc.MessageProducerService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.lang.Thread.sleep

@SpringBootTest(classes = [DevelopApplication::class])
class RpcTest {

    @Autowired
    private lateinit var producerService: MessageProducerService

    @Autowired
    private lateinit var consumerService: MessageConsumerService

    @Test
    fun sendMessageTest() = runBlocking {
        val message = "test123!!!"
        producerService.sendMessage(message)
    }
}