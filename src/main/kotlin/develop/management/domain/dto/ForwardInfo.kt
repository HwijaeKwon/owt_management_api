package develop.management.domain.dto

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class ForwardInfo(
        @Schema(description = "Owner of the stream")
        val owner: String,
        @Schema(description = "Type of the stream (\"webrtc\", \"streaming\", \"sip\" or \"analytics\")", example = "webrtc")
        val type: String,
        @ArraySchema(schema = Schema(description = "View label list"))
        val inViews: List<String>,
        @Schema(description = "External defined object", required = false)
        val attributes: Any)
