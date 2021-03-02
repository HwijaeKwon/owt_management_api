package develop.management.repository

import com.mongodb.client.result.DeleteResult
import develop.management.domain.document.Room
import develop.management.domain.dto.UpdateOptions

/**
 * 실제 DB와 관계없이 repository를 사용하기 위한 repository interface
 */
interface RoomRepository {
    suspend fun save(room: Room) : Room
    suspend fun findById(id: String) : Room?
    suspend fun findByIds(ids: List<String>): List<Room>
    suspend fun findAll() : List<Room>
    suspend fun existsById(id: String) : Boolean
    suspend fun deleteById(id: String) : DeleteResult
    suspend fun deleteByIds(ids: List<String>) : DeleteResult
    suspend fun deleteAll() : DeleteResult
    suspend fun updateById(id: String, updateOptions: UpdateOptions): Room?
}