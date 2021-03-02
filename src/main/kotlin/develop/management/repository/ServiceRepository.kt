package develop.management.repository

import com.mongodb.client.result.DeleteResult
import develop.management.domain.document.Service

/**
 * 실제 DB와 관계없이 repository를 사용하기 위한 repository interface
 */
interface ServiceRepository {
    suspend fun save(service: Service) : Service
    suspend fun existsById(id: String) : Boolean
    suspend fun existsByName(name: String) : Boolean
    suspend fun findById(id: String) : Service?
    suspend fun findByName(name: String) : List<Service>
    suspend fun findAll() : List<Service>
    suspend fun deleteById(id: String) : DeleteResult
    suspend fun deleteAll() : DeleteResult
}