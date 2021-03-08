package develop.management.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 Streaming-out 부분 참고
 */
data class SubscriptionControlInfo(
        @Schema(description = "Operation of subscription", nullable = false, required = true, example = "replace")
        val op: String,
        @Schema(description = "Path of subscription", nullable = false, required = true, example = "/media/audio/from")
        val path: String,
        @Schema(description = "Value of subscription", nullable = false, required = true)
        val value: Any) {
        //path example: /media/audio/from, /media/video/from, /media/video/parameters/resolution, /media/video/parameters/framerate, /media/video/parameters/bitrate, /media/video/parameters/keyFrameInterval
}
