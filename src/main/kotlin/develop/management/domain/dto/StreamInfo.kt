package develop.management.domain.dto

import develop.management.domain.MediaInfo
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Client에게 stream 정보를 전달하기 위한 dto 클래스
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 Streams 부분 참고
 */
data class StreamInfo(
        @Schema(description = "Unique identifier of the stream", nullable = false)
        val id: String,
        @Schema(description = "Stream type (\"forward\" or \"mixed\")", nullable = false, example = "forward")
        val type: String,
        @Schema(description = "Media info", nullable = false, implementation = MediaInfo::class)
        val media: MediaInfo,
        @Schema(description = "Stream information (\"MixedInfo\" or \"ForwardInfo\"", nullable = false)
        var info: Any) {

}
