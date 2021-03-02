package develop.management.repository.mongo

import com.mongodb.client.result.DeleteResult
import develop.management.domain.document.Key
import develop.management.repository.KeyRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
@Primary
class MongoKeyRepository(private val template: ReactiveMongoTemplate): KeyRepository {

    override suspend fun save(key: Key): Key {
        return template.save(key).awaitSingle()
    }

    override suspend fun findById(id: Number): Key? {
        return template.findById(id, Key::class.java).awaitSingleOrNull()
    }

    override suspend fun findAll(): List<Key> {
        return template.findAll(Key::class.java).collectList().awaitSingle()
    }

    override suspend fun existsById(id: Number): Boolean {
        val criteria = Criteria.where("_id").`is`(id)
        val query = Query(criteria)
        return template.exists(query, Key::class.java).awaitSingle()
    }

    override suspend fun deleteById(id: Number): DeleteResult {
        val criteria = Criteria.where("_id").`is`(id)
        val query = Query(criteria)
        return template.remove(query, Key::class.java).awaitSingle()
    }

    override suspend fun deleteAll(): DeleteResult {
        return template.remove(Query(), Key::class.java).awaitSingle()
    }
}