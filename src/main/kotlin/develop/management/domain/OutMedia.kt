package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * mediaOut object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Streaming-outs 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class OutMedia(
    @Schema(description = "Audio info of streaming out. StreamingOutAudio | false", nullable = false, required = true, implementation = StreamingOutAudio::class)
    val audio: Any,
    @Schema(description = "Video info of streaming out. StreamingOutVideo | false", nullable = false, required = true, implementation = StreamingOutVideo::class)
    val video: Any) {

    data class StreamingOutAudio(
            @Schema(description = "Unique identifier of streaming out audio", nullable = false)
            val from: String,
            @Schema(description = "Audio format of streaming out", nullable = true, defaultValue = "null", implementation = AudioFormat::class)
            val format: AudioFormat? = null)

    data class StreamingOutVideo(
            @Schema(description = "Unique identifier of streaming out video", nullable = false)
            val from: String,
            @Schema(description = "Video format of streaming out video", nullable = true, defaultValue = "null", implementation = VideoFormat::class)
            val format: VideoFormat? = null,
            @Schema(description = "Video parameter specification of streaming out video", nullable = false, implementation = VideoParametersSpecification::class)
            val parameters: VideoParametersSpecification)
}
