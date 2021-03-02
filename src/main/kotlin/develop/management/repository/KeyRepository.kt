package develop.management.repository

import com.mongodb.client.result.DeleteResult
import develop.management.domain.document.Key

/**
 * 실제 DB와 관계없이 repository를 사용하기 위한 repository interface
 */
interface KeyRepository {
    suspend fun save(key: Key): Key
    suspend fun findById(id: Number): Key?
    suspend fun findAll() : List<Key>
    suspend fun existsById(id: Number) : Boolean
    suspend fun deleteById(id: Number) : DeleteResult
    suspend fun deleteAll() : DeleteResult
}