package develop.management.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 사용자 권한 업데이트 요청시 client가 전달하는 데이터
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Participants 참고
 * op, path, value -> required
 */
data class PermissionUpdate(
    @Schema(description = "Operation of publication", nullable = false, required = true, example = "replace")
    val op: String,
    @Schema(description = "Path of publication", nullable = false, required = true, example = "/permission/publish/audio")
    val path: String,
    @Schema(description = "Value of publication", nullable = false, required = true)
    val value: Boolean) {
    //path example: /permission/publish/audio, /permission/publish/video, /permission/subscribe/audio, /permission/subscribe/video
}