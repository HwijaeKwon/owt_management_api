package develop.management.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 SipCalls 부분 참고
 */
data class MediaOutControlInfo(
        @Schema(description = "Operation of sip call media out", nullable = false, required = true, example = "replace")
        val op: String,
        @Schema(description = "Path of sip call media out", nullable = false, required = true, example = "/media/audio/from")
        val path: String,
        @Schema(description = "Value of sip call media out", nullable = false, required = true)
        val value: Any) {
        //path example: /output/media/video/parameters/resolution, /output/media/video/parameters/resolution, /output/media/video/parameters/framerate, /output/media/video/parameters/bitrate, /output/media/video/parameters/keyFrameInterval
}
