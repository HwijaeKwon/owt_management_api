package develop.management.domain.document

import develop.management.util.cipher.Cipher
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * key값을 저장하는 document class
 */

@Document(collection = "key")
class Key private constructor(private var key: String) {

    companion object {
        /**
         * key를 생성한다
         */
        fun createKey(): Key {
            val key = Cipher.encodeHexByteArray(Cipher.generateByteArray(64))
            return Key(key)
        }
    }

    //String <-> ObjectId 전환. MongoDB에서 생성되는 primary key
    @Id
    private var _id : Number = 0

    fun getId(): Number = this._id
    fun getKey(): String = this.key

    fun updateKey(key: String) {
        this.key = key
    }
}