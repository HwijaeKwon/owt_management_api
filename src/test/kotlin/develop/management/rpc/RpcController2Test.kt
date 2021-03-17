package develop.management.rpc

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import reactor.kotlin.core.publisher.toMono
import java.lang.Thread.sleep
import java.time.Duration

@ActiveProfiles("test")
@SpringBootTest(classes = [RpcController::class])
//@ContextConfiguration(initializers = [RpcController2Test.Initializer::class])
class RpcController2Test {

   /* companion object {
        val rabbit = RabbitMQContainer("rabbitmq:3-management")
                .withExposedPorts(5672, 15672)
                .withVhost("/")
                //.withUser("test", "test")
                //.withAdminPassword("test")
                //.withPermission("/", "guest", ".*", ".*", ".*")
                .apply { this.start() }!!
    }

    @AfterEach
    fun close() {
        rabbit.stop()
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            val values = TestPropertyValues.of(
                    "spring.rabbitmq.host: " + rabbit.containerIpAddress,
                    "spring.rabbitmq.port: " + rabbit.getMappedPort(5672),
                    "spring.rabbitmq.username: " + rabbit.adminUsername,
                    "spring.rabbitmq.password: " + rabbit.adminPassword,
            )
            values.applyTo(applicationContext)
            println("rabbit.containerIpAddress = ${rabbit.containerIpAddress}")
            println("rabbit.port = ${rabbit.getMappedPort(15672)}")
            println("rabbit.adminUsername = ${rabbit.adminUsername}")
            println("rabbit.adminPassword = ${rabbit.adminPassword}")
        }
    }*/

    @Autowired
    private lateinit var rpcController: RpcController

    @Test
    fun test() = runBlocking {
        val (stream, corrID) = rpcController.sendMessage("test", "test", JSONArray())
        val (data, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
    }
}