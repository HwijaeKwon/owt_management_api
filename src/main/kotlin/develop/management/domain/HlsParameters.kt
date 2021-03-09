package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * mediaOut object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Streaming-outs 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class HlsParameters(
    @Schema(description = "Method of hls parameters. \"PUT\" or \"POST\"", nullable = false)
    val method: String,
    @Schema(description = "HlsTime of hls parameters. HlsTime | false", nullable = true, defaultValue = "null")
    val hlsTime: Number? = null,
    @Schema(description = "HlsListSize of hls parameters. HlsListSize | false", nullable = true, defaultValue = "null")
    val hlsListSize: Number? = null) {
}
