package develop.management.repository.mongo

import kotlinx.coroutines.delay
import org.springframework.data.mongodb.MongoTransactionException
import org.springframework.data.mongodb.UncategorizedMongoDbException
import kotlin.random.Random

/**
 * Transactional operator에 재시도 로직을 추가한 클래스
 * @param include : retry할 exception class array
 * @param times: 최대 재시도 횟수
 * @param initialDelay (milliseconds)
 * @param maxDelay (milliseconds)
 */

class RetryOperation(val times: Int = 5,
                     val initialDelay: Long = 100,
                     val maxDelay: Long = 1000,
                     val include: Array<Class<out Throwable>> = arrayOf(UncategorizedMongoDbException::class.java, MongoTransactionException::class.java)) {

    /**
     * action에서 exception이 발생하였을 때 재시도를 하는 함수
     */

    suspend fun <T> execute (action: suspend () -> T): T
    {
        var currentDelay = initialDelay
        repeat(times) {
            try {
                return action()
            } catch (e: Exception) {
                // you can log an error here and/or make a more finer-grained
                // analysis of the cause to see if retry is needed

                if(include.none{ it === e.javaClass }) throw e
            }
            delay(currentDelay)
            currentDelay = (currentDelay * Random.nextInt(0, (maxDelay/initialDelay).toInt())).coerceAtMost(maxDelay)
        }
        return action() // last attempt
    }
}