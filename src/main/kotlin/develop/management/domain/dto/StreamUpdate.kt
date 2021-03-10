package develop.management.domain.dto

import develop.management.domain.Region
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Client가 stream 정보를 업데이트하기 위해 사용하는 dto 클래스
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 Streams 부분 참고
 */
data class StreamUpdate(
        @Schema(description = "Operation (\"add\", \"remove\", \"replace\")", nullable = false, required = true)
        val op: String,
        @Schema(description = "Path (\"/info/inViews\", \"/media/audio/status\", \"/media/video/status\", \"/info/layout/{regionIndex}/stream\", \"/info/layout\")", nullable = false, required = true)
        val path: String,
        @Schema(description = "Value of updates (MixUpdate, StatusUpdate, RegionUpdate, LayoutUpdate", nullable = false, required = true)
        val value: Any) {
}

data class StreamRegion(
        @Schema(description = "Unique identifier of the stream", nullable = true, required = false)
        val stream: String? = null,
        @Schema(description = "Region of the stream", nullable = false, required = true, implementation = Region::class)
        val region: Region)