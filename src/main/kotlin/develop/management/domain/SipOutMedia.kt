package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ SipCall 부분 참고
 */
data class SipOutMedia(
    @Schema(description = "Audio info for sipout", nullable = false, required = true, implementation = SipOutAudio::class)
    val audio: SipOutAudio,
    @Schema(description = "Video info for sipout. SipOutVideo or false", nullable = false, required = true, implementation = SipOutVideo::class)
    val video: Any) {

    data class SipOutAudio(
        @Schema(description = "Unique identifier for stream", nullable = false, required = true)
        val from: String)

    data class SipOutVideo(
        @Schema(description = "Unique identifier for stream", nullable = false, required = true)
        val from: String,
        @Schema(description = "Video parameters for sip out", nullable = false, required = true, implementation = VideoParametersSpecification::class)
        val parameters: VideoParametersSpecification)
}