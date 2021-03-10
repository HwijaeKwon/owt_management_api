package develop.management.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * room 생성 및 업데이트 시 client가 전달하는 데이터를 담은 클래스
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Participants 참고
 */
data class ParticipantDetail(
    @Schema(description = "Unique identifier of the participant", nullable = false)
    val _id: String,
    @Schema(description = "Unique identifier of the participant", nullable = false)
    val role: String,
    @Schema(description = "User id of the participant", nullable = false)
    val user: String,
    @Schema(description = "Permission of the participant", nullable = false, implementation = Permission::class)
    val permission: Permission) {}

data class Permission(
    @Schema(description = "Permission of publication", nullable = false, implementation = Publish::class)
    val publish: Publish,
    @Schema(description = "Permission of subscription", nullable = false, implementation = Subscribe::class)
    val subscribe: Subscribe) {
    data class Publish(val audio: Boolean, val video: Boolean) {}
    data class Subscribe(val audio: Boolean, val video: Boolean) {}
}