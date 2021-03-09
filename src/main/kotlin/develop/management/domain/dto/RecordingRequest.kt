package develop.management.domain.dto

import develop.management.domain.MediaSubOptions
import io.swagger.v3.oas.annotations.media.Schema

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 recordings 부분 참고
 */
data class RecordingRequest(
        @Schema(description = "Container type of the recording. \"auto\" | \"mkv\" | \"mp4\"", nullable = false, required = true, example = "auto")
        val container: String,
        @Schema(description = "Media subscription options of the recordings", nullable = false, required = true, implementation = MediaSubOptions::class)
        val media: MediaSubOptions)
