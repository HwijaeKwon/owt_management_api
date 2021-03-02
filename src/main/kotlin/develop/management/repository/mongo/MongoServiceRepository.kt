package develop.management.repository.mongo

import com.mongodb.client.result.DeleteResult
import develop.management.domain.document.Service
import develop.management.repository.ServiceRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
@Primary
class MongoServiceRepository(private val template: ReactiveMongoTemplate) : ServiceRepository {

    override suspend fun save(service: Service): Service {
        return template.save(service).awaitSingle()
    }

    override suspend fun existsById(id: String): Boolean {
        val criteria = Criteria.where("_id").`is`(id)
        val query = Query(criteria)
        template.exists(query, Service::class.java)
        return template.exists(query, Service::class.java).awaitSingle()
    }

    override suspend fun existsByName(name: String): Boolean {
        val criteria = Criteria.where("name").`is`(name)
        val query = Query(criteria)
        return template.exists(query, Service::class.java).awaitSingle()
    }

    override suspend fun findById(id: String): Service? {
        return template.findById(id, Service::class.java).awaitSingleOrNull()
    }

    override suspend fun findByName(name: String): List<Service> {
        val criteria = Criteria.where("name").`is`(name)
        val query = Query(criteria)
        return template.find(query, Service::class.java).collectList().awaitSingle()
    }

    override suspend fun findAll(): List<Service> {
        return template.findAll(Service::class.java).collectList().awaitSingle()
    }

    override suspend fun deleteById(id: String): DeleteResult {
        val criteria = Criteria.where("_id").`is`(id)
        val query = Query(criteria)
        return template.remove(query, Service::class.java).awaitSingle()
    }

    override suspend fun deleteAll(): DeleteResult {
        return template.remove(Query(), Service::class.java).awaitSingle()
    }
}