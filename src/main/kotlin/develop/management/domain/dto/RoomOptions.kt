package develop.management.domain.dto

import develop.management.domain.enum.RoomConfig
import develop.management.domain.*
import io.swagger.v3.oas.annotations.media.Schema

/**
 * room 생성 및 업데이트 시 client가 전달하는 데이터를 담은 클래스
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * 위 내용에서 몇 가지 속성들이 더 추가되었다
 */
sealed class RoomOptions(
    open val name: String?,
    open val participantLimit: Number?,
    open val inputLimit: Number?,
    open val roles: List<Role>?,
    open val views: List<View>?,
    open val mediaIn: MediaIn?,
    open val mediaOut: MediaOut?,
    open val transcoding: Transcoding?,
    open val notifying: Notifying?,
    open val sip: Sip?) {
}

/**
 * room 생성 시 client가 전달하는 데이터를 담은 클래스
 * name : required
 */
class CreateOptions(
    @Schema(description = "Name of the room", nullable = false, required = false)
    override val name: String = "",
    @Schema(description = "Participant limit of the room", nullable = false, required = false)
    override val participantLimit: Number = RoomConfig.DEFAULT_CONFIG_PARTICIPANTLIMIT.get() as Number,
    @Schema(description = "Input limit of the room", nullable = false, required = false)
    override val inputLimit: Number = RoomConfig.DEFAULT_CONFIG_INPUTLIMIT.get() as Number,
    @Schema(description = "Roles of the room", nullable = false, required = false)
    override val roles: List<Role> = RoomConfig.DEFAULT_CONFIG_ROLES.get() as List<Role>,
    @Schema(description = "Views of the room", nullable = false, required = false)
    override val views: List<View> = RoomConfig.DEFAULT_CONFIG_VIEWS.get() as List<View>,
    @Schema(description = "MediaIn of the room", nullable = false, required = false)
    override val mediaIn: MediaIn = RoomConfig.DEFAULT_CONFIG_MEDIAIN.get() as MediaIn,
    @Schema(description = "MediaOut of the room", nullable = false, required = false)
    override val mediaOut: MediaOut = RoomConfig.DEFAULT_CONFIG_MEDIAOUT.get() as MediaOut,
    @Schema(description = "Transcoding of the room", nullable = false, required = false)
    override val transcoding: Transcoding = RoomConfig.DEFAULT_CONFIG_TRANSCODING.get() as Transcoding,
    @Schema(description = "Notifying of the room", nullable = false, required = false)
    override val notifying: Notifying = RoomConfig.DEFAULT_CONFIG_NOTIFYING.get() as Notifying,
    @Schema(description = "Sip of the room", nullable = false, required = false)
    override val sip: Sip = RoomConfig.DEFAULT_CONFIG_SIP.get() as Sip)
    : RoomOptions(
        name,
        participantLimit,
        inputLimit,
        roles,
        views,
        mediaIn,
        mediaOut,
        transcoding,
        notifying,
        sip) {
}

/**
 * room 업데이트 시 client가 전달하는 데이터를 담은 클래스
 */
data class UpdateOptions(
    @Schema(description = "Name of the room", nullable = false, required = false, defaultValue = "null")
    override val name: String? = null,
    @Schema(description = "Participant limit of the room", nullable = false, required = false, defaultValue = "null")
    override val participantLimit: Number? = null,
    @Schema(description = "Input limit of the room", nullable = false, required = false, defaultValue = "null")
    override val inputLimit: Number? = null,
    @Schema(description = "Roles of the room", nullable = false, required = false, defaultValue = "null")
    override val roles: List<Role>? = null,
    @Schema(description = "Views of the room", nullable = false, required = false, defaultValue = "null")
    override val views: List<View>? = null,
    @Schema(description = "MediaIn of the room", nullable = false, required = false, defaultValue = "null")
    override val mediaIn: MediaIn? = null,
    @Schema(description = "MediaOut of the room", nullable = false, required = false, defaultValue = "null")
    override val mediaOut: MediaOut? = null,
    @Schema(description = "Transcoding of the room", nullable = false, required = false, defaultValue = "null")
    override val transcoding: Transcoding? = null,
    @Schema(description = "Notifying of the room", nullable = false, required = false, defaultValue = "null")
    override val notifying: Notifying? = null,
    @Schema(description = "Sip of the room", nullable = false, required = false, defaultValue = "null")
    override val sip: Sip? = null)
    : RoomOptions(name, participantLimit, inputLimit, roles, views, mediaIn, mediaOut, transcoding, notifying, sip) {
}
