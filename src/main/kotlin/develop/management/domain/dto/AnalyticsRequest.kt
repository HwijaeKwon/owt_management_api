package develop.management.domain.dto

import develop.management.domain.MediaSubOptions
import io.swagger.v3.oas.annotations.media.Schema

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 SipCalls 부분 참고
 */
data class AnalyticsRequest(
        @Schema(description = "Algorithm id of analytics", nullable = false, required = true)
        val algorithm: String,
        @Schema(description = "Media of analytics", nullable = false, required = true, implementation = MediaSubOptions::class)
        val media: MediaSubOptions) {
}
