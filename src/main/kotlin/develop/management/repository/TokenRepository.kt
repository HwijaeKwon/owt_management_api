package develop.management.repository

import com.mongodb.client.result.DeleteResult
import develop.management.domain.document.Token

/**
 * 실제 DB와 관계없이 repository를 사용하기 위한 repository interface
 */
interface TokenRepository {
    suspend fun save(token: Token): Token
    suspend fun findById(id: String) : Token?
    suspend fun findAll() : List<Token>
    suspend fun existsById(id: String) : Boolean
    suspend fun deleteById(id: String) : DeleteResult
    suspend fun deleteAll() : DeleteResult
}