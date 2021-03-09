package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * mediaOut object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Streaming-outs 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class DashParameters(
    @Schema(description = "Method of dash parameters. \"PUT\" or \"POST\"", nullable = false)
    val method: String,
    @Schema(description = "DashSegDuration of dash parameters. DashSegDuration | false", nullable = true, defaultValue = "null")
    val dashSegDuration: Number? = null,
    @Schema(description = "DashWindowSize of dash parameters. DashWindowSize | false", nullable = true, defaultValue = "null")
    val dashWindowSize: Number? = null) {
}
