package develop.management.domain

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

/**
 * mediaOut object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class MediaOut(
    @ArraySchema(schema = Schema(description = "Audio format of media out", nullable = false, required = true, implementation = AudioFormat::class))
    val audio: List<AudioFormat>,
    @Schema(description = "Video of media out", nullable = false, required = true, implementation = Video::class)
    val video: Video) {

    data class Video(
        @ArraySchema(schema = Schema(description = "Video format of media out", nullable = false, required = true, implementation = VideoFormat::class))
        val format: List<VideoFormat>,
        @Schema(description = "Parameter of media out video", nullable = false, required = true, implementation = Parameters::class)
        val parameters: Parameters) {

        data class Parameters(
            @ArraySchema(schema = Schema(description = "Resolution of media out video", nullable = false, required = true, example = "[\"x3/4\", \"x2/3\", ... \"cif\"]"))
            val resolution: List<String>,
            @ArraySchema(schema = Schema(description = "Framerate of media out video", nullable = false, required = true, example = "[5, 15, 24, 30, 48, 60]"))
            val framerate: List<Number>,
            @ArraySchema(schema = Schema(description = "Bitrate of media out video", nullable = false, required = true, example = "[500, 1000, ... ]"))
            val bitrate: List<String>,
            @ArraySchema(schema = Schema(description = "KeyFrameInterval of media out video", nullable = false, required = true, example = "[100, 30, 5, 2, 1]"))
            val keyFrameInterval: List<Number>) {
        }
    }
}
