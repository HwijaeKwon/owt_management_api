package develop.management.domain.dto

import develop.management.domain.MediaInfo
import develop.management.domain.SipOutMedia
import io.swagger.v3.oas.annotations.media.Schema
import java.sql.Connection

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 SipCalls 부분 참고
 */
data class SipCallRequest(
        @Schema(description = "Peer url of sipcall", nullable = false, required = true)
        val peerURI: String,
        @Schema(description = "Media of streaming in", nullable = false, required = true, implementation = MediaIn::class)
        val mediaIn: MediaIn,
        @Schema(description = "Media out of streaming in", nullable = false, required = true, implementation = SipOutMedia::class)
        var mediaOut: SipOutMedia) {

        data class MediaIn(
                @Schema(description = "Audio must be true for sip calls", nullable = false, required = false, defaultValue = "true")
                val audio: Boolean = true,
                @Schema(description = "Video activation for sip calls", nullable = false, required = true)
                val video: Boolean)
}
