package develop.management.rpc

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig
import io.arivera.oss.embedded.rabbitmq.PredefinedVersion
import io.arivera.oss.embedded.rabbitmq.RabbitMqEnvVar
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@Profile("test")
@TestConfiguration
class RabbitMqConfig {

    @Autowired
    private lateinit var environment: Environment

    @Bean
    fun embeddedRabbitMq(): EmbeddedRabbitMq {
        println("RabbitMqConfig.embeddedRabbitMq")
        val host = environment.getProperty("spring.rabbitmq.host", String::class.java, "localhost")
        val port = environment.getProperty("spring.rabbitmq.port", Int::class.java, 5672)

        val envVar = mutableMapOf<String, String>()
        envVar["RABBITMQ_NODE_IP_ADDRESS"] = host
        envVar["RABBITMQ_NODE_PORT"] = port.toString()

        val config = EmbeddedRabbitMqConfig.Builder()
                .version(PredefinedVersion.V3_6_5)
                .envVars(envVar)
                .build()

        return EmbeddedRabbitMq(config)
    }
}