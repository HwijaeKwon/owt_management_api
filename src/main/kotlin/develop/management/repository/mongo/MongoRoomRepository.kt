package develop.management.repository.mongo

import com.mongodb.client.result.DeleteResult
import develop.management.domain.document.Room
import develop.management.domain.dto.UpdateOptions
import develop.management.repository.RoomRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*
import kotlin.reflect.full.memberProperties

@Repository
@Primary
class MongoRoomRepository(private val template: ReactiveMongoTemplate) : RoomRepository {

    override suspend fun save(room: Room): Room {
        return template.save(room).awaitSingle()
    }

    override suspend fun findById(id: String): Room? {
        return template.findById(id, Room::class.java).awaitSingleOrNull()
    }

    override suspend fun findByIds(ids: List<String>): List<Room> {
        val criteria = Criteria.where("_id").`in`(ids)
        val query = Query(criteria)
        return template.find(query, Room::class.java).collectList().awaitSingle()
    }

    override suspend fun findAll(): List<Room> {
        return template.findAll(Room::class.java).collectList().awaitSingle()
    }

    override suspend fun existsById(id: String): Boolean {
        val criteria = Criteria.where("_id").`in`(id)
        val query = Query(criteria)
        return template.exists(query, Room::class.java).awaitSingle()
    }

    override suspend fun deleteById(id: String): DeleteResult {
        val criteria = Criteria.where("_id").`is`(id)
        val query = Query(criteria)
        return template.remove(query, Room::class.java).awaitSingle()
    }

    override suspend fun deleteByIds(ids: List<String>): DeleteResult {
        val criteria = Criteria.where("_id").`in`(ids)
        val query = Query(criteria)
        return template.remove(query, Room::class.java).awaitSingle()
    }

    override suspend fun deleteAll(): DeleteResult {
        return template.remove(Query(), Room::class.java).awaitSingle()
    }

    override suspend fun updateById(id: String, updateOptions: UpdateOptions): Room? {
        val criteria = Criteria.where("_id").`in`(id)
        val query = Query(criteria)
        val updates = Update()
        updateOptions.javaClass.kotlin.memberProperties.filter { it.get(updateOptions) != null }.forEach {
            updates.set(it.name, it.get(updateOptions))
        }

        return template.findAndModify(query, updates, FindAndModifyOptions().returnNew(true), Room::class.java).onErrorResume {
            if(it is NoSuchElementException) return@onErrorResume Mono.empty()
            else throw it
        }.awaitSingleOrNull()
    }
}