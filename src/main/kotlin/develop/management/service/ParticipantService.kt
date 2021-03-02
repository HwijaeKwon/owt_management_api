package develop.management.service

import develop.management.domain.dto.ParticipantDetail
import develop.management.domain.dto.Permission
import develop.management.domain.dto.PermissionUpdate
import org.springframework.stereotype.Service

/**
 * 사용자 관련 비즈니스 로직을 담당하는 service class
 */
@Service
class ParticipantService {

    /**
     * ManagementAPI
     * Conference에서 사용자를 조회한다
     */
    suspend fun findOneFromConference(roomId: String, participantId: String): ParticipantDetail? {
        //Todo: rpc를 통해 participant 정보를 가져와야 한다
        // Rpc error시 exception을 던져야 한다
        return ParticipantDetail("id", "role", "user", Permission(Permission.Publish(audio = true, video = true), Permission.Subscribe(audio = true, video = true)))
    }

    /**
     * ManagementAPI
     * Conference에서 특정 방의 모든 사용자를 조회한다
     */
    suspend fun findAllFromConference(roomId: String): List<ParticipantDetail> {
        //Todo: rpc를 통해 participant 정보를 가져와야 한다
        // Rpc error시 exception을 던져야 한다
        return listOf(ParticipantDetail("id", "role", "user", Permission(Permission.Publish(audio = true, video = true), Permission.Subscribe(audio = true, video = true))))
    }

    /**
     * ManagementAPI
     * 특정 room의 사용자 권한을 conference에 업데이트 한다
     */
    suspend fun updateToConference(roomId: String, participantId: String, permissionUpdate: PermissionUpdate): ParticipantDetail? {
        //Todo: rpc를 통해 participant 정보를 업데이트해야 한다
        // Rpc error시 exception을 던져야 한다
        return ParticipantDetail("id", "role", "user", Permission(Permission.Publish(audio = true, video = true), Permission.Subscribe(audio = true, video = true)))
    }

    /**
     * ManagementAPI
     * Conference에서 특정 room의 특정 participant를 제거한다
     */
    suspend fun deleteFromConference(roomId: String, participantId: String) {
        //Todo: rpc를 통해 participant 정보를 삭제해야 한다
        // Rpc error시 exception을 던져야 한다
    }
}