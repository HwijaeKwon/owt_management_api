package develop.management.rpc

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.messaging.Source
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.messaging.support.MessageBuilder.*
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit

@Testcontainers
@SpringBootTest(classes = [RpcService::class, RpcController::class])
internal class RpcServiceTest {

    class MyContainer(imageName: String): GenericContainer<MyContainer>(imageName)

    @Container
    public val rabbit = MyContainer("rabbitmq:3-management")
}