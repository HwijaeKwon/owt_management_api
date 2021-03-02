package develop.management.domain.dto

import develop.management.domain.*
import develop.management.domain.document.Room

/**
 * client에게 방 정보를 전달하기 위한 데이터 클래스
 */
data class RoomInfo(val id: String,
                    val name: String,
                    val participantLimit: Number,
                    val inputLimit: Number,
                    val roles: List<Role>,
                    val views: List<View>,
                    val mediaIn: MediaIn,
                    val mediaOut: MediaOut,
                    val transcoding: Transcoding,
                    val notifying: Notifying,
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