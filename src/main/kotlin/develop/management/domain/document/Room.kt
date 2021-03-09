package develop.management.domain.document

import develop.management.domain.*
import develop.management.domain.enum.Room.*
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.RoomOptions

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * room 관련 정보를 저장하는 document class
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 */
@Document(collection = "rooms")
class Room private constructor(private var name: String,
                               private var participantLimit: Number = DEFAULT_PARTICIPANTLIMIT.get() as Number,
                               private var inputLimit: Number = DEFAULT_INPUTLIMIT.get() as Number,
                               private var roles: List<Role> = DEFAULT_ROLES.get() as List<Role>,
                               private var views: List<View> = DEFAULT_VIEWS.get() as List<View>,
                               private var mediaIn: MediaIn = DEFAULT_MEDIAIN.get() as MediaIn,
                               private var mediaOut: MediaOut = DEFAULT_MEDIAOUT.get() as MediaOut,
                               private var transcoding: Transcoding = DEFAULT_TRANSCODING.get() as Transcoding,
                               private var notifying: Notifying = DEFAULT_NOTIFYING.get() as Notifying,
                               private var sip: Sip = DEFAULT_SIP.get() as Sip) {

    companion object {
        fun create(roomConfig: RoomConfig): Room {
            val name = roomConfig.name
            val options = roomConfig.options
            return Room(
                name,
                options.participantLimit,
                options.inputLimit,
                options.roles,
                options.views,
                options.mediaIn,
                options.mediaOut,
                options.transcoding,
                options.notifying,
                options.sip)
        }
    }

    //String <-> ObjectId 전환. MongoDB에서 생성되는 primary key
    @Id
    private lateinit var _id : String

    fun getId(): String = this._id
    fun getName(): String = this.name
    fun getParticipantLimit(): Number = this.participantLimit
    fun getInputLimit(): Number = this.inputLimit
    fun getRoles(): List<Role> = this.roles
    fun getViews(): List<View> = this.views
    fun getMediaIn(): MediaIn = this.mediaIn
    fun getMediaOut(): MediaOut = this.mediaOut
    fun getTranscoding(): Transcoding = this.transcoding
    fun getNotifying(): Notifying = this.notifying
    fun getSip(): Sip = this.sip

    /**
     * 현재 room의 정보를 인자로 들어온 options의 정보로 갱신한다
     * 인자로 들어온 updates의 정보 중 null인 정보는 갱신하지 않는다
     */
    fun update(options: RoomOptions) {
        options.name?.let { this.name = it }
        options.participantLimit?.let { this.participantLimit = it }
        options.inputLimit?.let { this.inputLimit = it }
        //Todo: roles 객체는 완전하다고 가정한다. 체크해야 하나?
        options.roles?. let { this.roles = it }
        options.views?. let { this.views = it }
        options.mediaIn?. let { this.mediaIn = it }
        options.mediaOut?. let { this.mediaOut = it }
        options.transcoding?. let { this.transcoding = it }
        options.notifying?. let { this.notifying = it }
        options.sip?. let { this.sip = it }
    }
}