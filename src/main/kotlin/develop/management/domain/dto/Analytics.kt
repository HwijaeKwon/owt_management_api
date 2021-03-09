package develop.management.domain.dto

import develop.management.domain.VideoFormat
import io.swagger.v3.oas.annotations.media.Schema

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 Analytics 부분 참고
 */
data class Analytics(
        @Schema(description = "Unique identifier of the analytics", nullable = false)
        val id: String,
        @Schema(description = "Analytics", nullable = false, implementation = Analytics::class)
        val analytics: Analytics,
        @Schema(description = "Media of analytics", nullable = false, implementation = Media::class)
        var media: Media) {

        data class Analytics(
                @Schema(description = "Algorithm of analytics")
                val algorithm: String)

        data class Media(
                @Schema(description = "Video format of analytics media")
                val format: VideoFormat,
                @Schema(description = "Source stream id of analytics media")
                val from: String)
}
