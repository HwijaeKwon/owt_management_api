package develop.management.domain.dto

import develop.management.domain.HlsParameters
import develop.management.domain.MediaInfo
import develop.management.domain.OutMedia
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Client에게 stream 정보를 전달하기 위한 dto 클래스
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 Streaming-outs 부분 참고
 */
data class StreamingOut(
        @Schema(description = "Unique identifier of the streaming out", nullable = false)
        val id: String,
        @Schema(description = "Out media of streaming out", nullable = false, implementation = OutMedia::class)
        val media: OutMedia,
        @Schema(description = "Protocol of streaming out. \"rtmp\", \"rtsp\", \"hls\" or \"dash\"", nullable = false, example = "rtmp")
        val protocol: String,
        @Schema(description = "Parameters of streaming out. HlsParameters | DashParameters", nullable = true, defaultValue = "null", implementation = HlsParameters::class)
        val parameters: Any? = null) {
}
