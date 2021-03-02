package develop.management.domain.document

import develop.management.domain.dto.ServiceConfig
import develop.management.util.cipher.Cipher
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * service 관련 정보를 저장하는 document class
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Services 참고
 */
@Document(collection = "service")
class Service private constructor(@Indexed(unique = true)private val name: String,
                                      private val key: String) {
    companion object {
        fun create(serviceConfig: ServiceConfig): Service {
            val encryptedKey = Cipher.encrypt(serviceConfig.key)
            return Service(serviceConfig.name, encryptedKey).also { it.encrypted = true }
        }
    }

    //String <-> ObjectId 전환
    @Id
    private lateinit var _id : String

    private var encrypted: Boolean = false

    private var rooms: MutableList<String> = mutableListOf()

    fun getId(): String = this._id
    fun getName(): String = this.name
    fun getEncrypted(): Boolean = this.encrypted
    fun getKey(): String = this.key
    fun getRooms(): List<String> = this.rooms
    fun addRoom(roomId: String) {
        this.rooms.add(roomId)
    }
    fun removeRoom(roomId: String) {
        this.rooms.remove(roomId)
    }
}