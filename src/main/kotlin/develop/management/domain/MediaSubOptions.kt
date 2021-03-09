package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * mediaOut object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Streaming-outs 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class MediaSubOptions(
    @Schema(description = "Audio info of media subscription out. Audio | false", nullable = false, required = true, implementation = Audio::class)
    val audio: Any,
    @Schema(description = "Video info of media subscription out. Video | false", nullable = false, required = true, implementation = Video::class)
    val video: Any) {

    data class Audio(
            @Schema(description = "Target stream id of audio media subscription options", nullable = false, required = true)
            val from: String,
            @Schema(description = "Audio format of media subscription options. AudioFormat", nullable = false, required = true, implementation = AudioFormat::class)
            val format: AudioFormat)

    data class Video(
            @Schema(description = "Target stream id of video media subscription options", nullable = false, required = true)
            val from: String,
            @Schema(description = "Video format of video media subscription options. VideoFormat", nullable = false, required = true, implementation = VideoFormat::class)
            val format: VideoFormat,
            @Schema(description = "Video parameters of video media subscription options. Parameters", nullable = false, required = true, implementation = Parameters::class)
            val parameters: Parameters) {

        data class Parameters(
                val resolution: Resolution,
                val framerate: Number? = null,
                val bitrate: String? = null,
                val keyFrameInterval: Number? = null)
    }
}
