package develop.management.repository.mongo

import com.mongodb.*
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.SessionSynchronization
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory

@Profile("dev")
@TestConfiguration
@PropertySource(value = ["classpath:application.yml"])
class TestReactiveMongoConfig(private val environment: Environment) {

    private val uri = environment.getProperty("spring.data.mongodb.uri", "mongodb://localhost:12345")
    private val ip = environment.getProperty("spring.data.mongodb.ip", String::class.java, "localhost")
    private val port = environment.getProperty("spring.data.mongodb.port", Int::class.java,12345)
    private val databaseName = environment.getProperty("spring.data.mongodb.database", "test")

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun mongoExecutable(): MongodExecutable {
        val starter = MongodStarter.getDefaultInstance()

        val mongodConfig = MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(Net(ip, port, false))
            .build()

        return starter.prepare(mongodConfig)
    }

    @Bean(destroyMethod = "close")
    fun reactiveMongoClient(): MongoClient {
        val connectionString = ConnectionString(uri)
        val settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        return MongoClients.create(settings)
    }

    @Bean
    fun reactiveMongoDatabaseFactory(reactiveMongoClient: MongoClient): SimpleReactiveMongoDatabaseFactory {
        return SimpleReactiveMongoDatabaseFactory(reactiveMongoClient, databaseName)
    }

    @Bean
    fun reactiveMongoTransactionManager(reactiveMongoDatabaseFactory: ReactiveMongoDatabaseFactory): ReactiveMongoTransactionManager {
        return ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory)
    }

    @Bean
    fun transactionalOperator(reactiveMongoTransactionManager: ReactiveMongoTransactionManager): TransactionalOperator {
        return TransactionalOperator.create(reactiveMongoTransactionManager)
    }

    @Bean
    fun reactiveMongoTemplate(reactiveMongoClient: MongoClient): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(reactiveMongoClient, databaseName).also {
            it.setSessionSynchronization(SessionSynchronization.ALWAYS)
        }
    }

    @Bean
    fun retryOperation(): RetryOperation {
        val times: Int = environment.getProperty("mongodb.retry.times", Int::class.java, 5)
        val initialDelay: Long = environment.getProperty("mongodb.retry.initialDelay", Long::class.java, 100)
        val maxDelay: Long = environment.getProperty("mongodb.retry.maxDelay", Long::class.java, 1000)
        return RetryOperation(times, initialDelay, maxDelay)
    }

    fun getDatabaseName(): String {
        return databaseName
    }
}