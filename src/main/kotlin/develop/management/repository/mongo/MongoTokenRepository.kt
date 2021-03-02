package develop.management.repository.mongo

import com.mongodb.client.result.DeleteResult
import develop.management.domain.document.Token
import develop.management.repository.TokenRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
@Primary
class MongoTokenRepository(private val template: ReactiveMongoTemplate) : TokenRepository {
    override suspend fun save(token: Token): Token {
        return template.save(token).awaitSingle()
    }

    override suspend fun findById(id: String): Token? {
        return template.findById(id, Token::class.java).awaitSingleOrNull()
    }

    override suspend fun findAll(): List<Token> {
        return template.findAll(Token::class.java).collectList().awaitSingle()
    }

    override suspend fun existsById(id: String): Boolean {
        val criteria = Criteria.where("_id").`is`(id)
        val query = Query(criteria)
        return template.exists(query, Token::class.java).awaitSingle()
    }

    override suspend fun deleteById(id: String): DeleteResult {
        val criteria = Criteria.where("_id").`is`(id)
        val query = Query(criteria)
        return template.remove(query, Token::class.java).awaitSingle()
    }

    override suspend fun deleteAll(): DeleteResult {
        return template.remove(Query(), Token::class.java).awaitSingle()
    }
}