package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * video format object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class VideoFormat(
    @Schema(description = "Codec of video", nullable = false, required = true, example = "h264, vp8, h265, vp9")
    val codec: String,
    @Schema(description = "Profile of video", nullable = true, required = true, example = "For h264 output only, CB, B, M, H")
    val profile: String?) {
}
