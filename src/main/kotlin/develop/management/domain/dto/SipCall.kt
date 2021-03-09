package develop.management.domain.dto

import develop.management.domain.MediaInfo
import develop.management.domain.SipOutMedia
import io.swagger.v3.oas.annotations.media.Schema
import java.sql.Connection

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 SipCalls 부분 참고
 */
data class SipCall(
        @Schema(description = "Unique identifier of the sipcall", nullable = false)
        val id: String,
        @Schema(description = "Type of sipcall. \"dial-in\" | \"dial-out\"", nullable = false, example = "dial-in")
        val type: String,
        @Schema(description = "Peer url of sipcall", nullable = false)
        var peer: String,
        @Schema(description = "Stream info of the sipcall", nullable = false, implementation = StreamInfo::class)
        val input: StreamInfo,
        @Schema(description = "Output of the sipcall", nullable = false, implementation = Output::class)
        val output: Output) {

        data class Output(
                @Schema(description = "Subscription id for sip calls", nullable = false)
                val id: String,
                @Schema(description = "Sip out media for sip calls", nullable = false, implementation = SipOutMedia::class)
                val media: SipOutMedia)
}
