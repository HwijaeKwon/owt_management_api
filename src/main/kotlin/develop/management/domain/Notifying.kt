package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * notifying object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
class Notifying(
    @Schema(description = "Whether enable notification for participantActivities", nullable = false, defaultValue = "true")
    val participantActivities: Boolean = true,
    @Schema(description = "Whether enable notification for streamChange", nullable = false, defaultValue = "true")
    val streamChange: Boolean = true) {
}
