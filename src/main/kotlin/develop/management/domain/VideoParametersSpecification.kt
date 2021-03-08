package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Streaming out 부분 참고
 */
data class VideoParametersSpecification(
    @Schema(description = "Resolution of streaming out", nullable = false, required = true, implementation = Resolution::class)
    val resolution: Resolution,
    @Schema(description = "Wanted framerate fps of streaming out", nullable = false, required = true)
    val framerate: Number,
    @Schema(description = "Wanted bitrate fps or wanted bitrate multiple")
    val bitrate: Any,
    @Schema(description = "Wanted key framerate interval seconds", nullable = false, required = true)
    val keyFrameInterval: Number) {
}