package develop.management.domain.dto

import develop.management.domain.*
import develop.management.domain.document.Room
import develop.management.domain.enum.RoomConfig
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

/**
 * client에게 방 정보를 전달하기 위한 데이터 클래스
 */
data class RoomInfo(
    @Schema(description = "Unique identifier of the room", nullable = false, required = true)
    val _id: String,
    @Schema(description = "Name of the room", nullable = false, required = true, example = "name")
    val name: String,
    @Schema(description = "Participant limit of the room", nullable = false, required = false, example = "10")
    val participantLimit: Number,
    @Schema(description = "Input limit of the room", nullable = false, required = true, minimum = "-1", example = "10")
    val inputLimit: Number,
    @ArraySchema(schema = Schema(description = "Role of the room", nullable = false, required = true, implementation = Role::class))
    val roles: List<Role>,
    @ArraySchema(schema = Schema(description = "View of the room", nullable = false, required = true, implementation = View::class))
    val views: List<View>,
    @Schema(description = "Media in of the room", nullable = false, required = true, implementation = MediaIn::class)
    val mediaIn: MediaIn,
    @Schema(description = "Media out of the room", nullable = false, required = true, implementation = MediaOut::class)
    val mediaOut: MediaOut,
    @Schema(description = "Transcoding of the room", nullable = false, required = true, implementation = Transcoding::class)
    val transcoding: Transcoding,
    @Schema(description = "Notifying of the room", nullable = false, required = true, implementation = Notifying::class)
    val notifying: Notifying,
    @Schema(description = "Sip of the room", nullable = false, required = true, implementation = Sip::class)
    val sip: Sip) {

    companion object {
        fun create(room: Room): RoomInfo {
            return RoomInfo(
                    room.getId(),
                    room.getName(),
                    room.getParticipantLimit(),
                    room.getInputLimit(),
                    room.getRoles(),
                    room.getViews(),
                    room.getMediaIn(),
                    room.getMediaOut(),
                    room.getTranscoding(),
                    room.getNotifying(),
                    room.getSip())
        }
    }
}