package develop.management.repository.mongo

import com.mongodb.*
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.SessionSynchronization
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.transaction.reactive.TransactionalOperator


@Configuration
@PropertySource(value = ["classpath:application.yml"])
class ReactiveMongoConfig(private val environment: Environment) {

    private val uri = environment.getProperty("spring.data.mongodb.uri", "mongodb://localhost:27017")
    private val databaseName = environment.getProperty("spring.data.mongodb.database", "test")
    //private val username = environment.getProperty("spring.data.mongodb.username", "test")
    //private val password = environment.getProperty("spring.data.mongodb.password", "test")

    @Bean
    fun reactiveMongoClient(): MongoClient {
        val connectionString = ConnectionString(uri)
        val settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            //.credential(MongoCredential.createCredential(username, databaseName, password.toCharArray()))
            .retryWrites(true)
            .readConcern(ReadConcern.MAJORITY)
            .writeConcern(WriteConcern.MAJORITY)
            .build()
        return MongoClients.create(settings)
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