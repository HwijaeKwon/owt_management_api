package develop.management.domain.dto

import develop.management.domain.HlsParameters
import develop.management.domain.MediaSubOptions
import io.swagger.v3.oas.annotations.media.Schema

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 Streaming-out 부분 참고
 */
data class StreamingOutRequest(
        @Schema(description = "Protocol of streaming out. \"rtmp\" | \"rtsp\" | \"hls\" | \"dash\"", nullable = true, required = true, example = "rtmp")
        val protocol: String? = null,
        @Schema(description = "Url of streaming out", nullable = false, required = true)
        val url: String,
        @Schema(description = "Parameters of streaming out. HlsParameters | DashParameters", nullable = true, required = false, defaultValue = "null", implementation = HlsParameters::class)
        var parameters: Any? = null,
        @Schema(description = "MediaSubOptions of streaming out", nullable = false, required = true, implementation = MediaSubOptions::class)
        val media: MediaSubOptions) {
}
