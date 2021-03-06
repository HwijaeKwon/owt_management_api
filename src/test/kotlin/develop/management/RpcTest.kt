package develop.management

import com.google.gson.Gson
import develop.management.rpc.MessageConsumerService
import develop.management.rpc.MessageProducerService
import develop.management.rpc.MessageService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.lang.Thread.sleep
import java.time.Duration

@SpringBootTest(classes = [DevelopApplication::class])
class RpcTest {

    @Autowired
    private lateinit var producerService: MessageProducerService

    @Autowired
    private lateinit var consumerService: MessageConsumerService

    @Autowired
    private lateinit var messageService: MessageService

    @Test
    fun sendMessageTest() = runBlocking {
        val message = Gson().toJson(TestMessage("test message"))
        producerService.sendMessage(message)
    }

    @Test
    fun sendMessageTest2() = runBlocking {
        val message = Gson().toJson(TestMessage("test message 2"))
        val reply_stream = messageService.sendMessage(message)
        val reply_str = reply_stream.blockFirst(Duration.ofMillis(3000))
        println("test result : $reply_str")
    }

    data class TestMessage(val message: String)
}