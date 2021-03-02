package develop.management.domain.document

import develop.management.domain.dto.TokenConfig
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * token 관련 정보를 저장하는 document class
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Token 참고
 */
@Document(collection = "token")
class Token private constructor(private val user: String,
                                private val role: String,
                                private val origin: Origin) {

    companion object {
        fun createToken(tokenConfig: TokenConfig): Token {
            return Token(tokenConfig.user, tokenConfig.role, tokenConfig.preference)
        }
    }

    //String <-> ObjectId 전환. MongoDB에서 생성되는 primary key
    @Id
    private lateinit var _id : String

    private var roomId: String = ""//room id (Room document)
    private var serviceId: String = ""//service id (Service document)
    private var code: String = ""
    private var secure: Boolean = false
    private var host: String = ""

    fun getId(): String = this._id
    fun getUser(): String = this.user
    fun getRole(): String = this.role
    fun getOrigin(): Origin = this.origin
    fun getRoomId(): String = this.roomId
    fun getServiceId(): String = this.serviceId
    fun getCode(): String = this.code
    fun getSecure(): Boolean = this.secure
    fun getHost(): String = this.host

    /**
     * 외부에서 token을 업데이트 한다
     */
    fun updateToken(roomId: String, serviceId: String, code: String, secure: Boolean, host: String) {
        this.roomId = roomId
        this.serviceId = serviceId
        this.code = code
        this.secure = secure
        this.host = host
    }

    class Origin(
        @Schema(description = "Isp of the token", nullable = true, required = false)
        val isp: String = "isp",
        @Schema(description = "Region of the token", nullable = true, required = false)
        val region: String = "region") {

        //default 값이 isp, region으로 설정되어 있다
        //token request 데이터자체가 업데이트 되는 일이 없다.
        //그러므로 null을 받고, 나중에 null이 아닌 부분에 defaul값을 채울 필요가 없다
        //처음부터 default값으로 채워둬도 된다
    }
}