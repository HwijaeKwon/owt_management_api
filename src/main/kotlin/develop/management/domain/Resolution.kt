package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * video resolution
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class Resolution(
    @Schema(description = "Resolution width")
    val width: Number,
    @Schema(description = "Resolution height")
    val height: Number) {
}