package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * transcoding object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
class Transcoding(
    @Schema(description = "Whether audio transcoding is allowed", nullable = false, defaultValue = "true")
    audio: Boolean = true,
    @Schema(description = "Transcoding of video", nullable = false, implementation = Video::class)
    video: Video = Video(true, Video.Parameters(true, true, true, true))) {

    class Video(
        @Schema(description = "Whether video transcoding is allowed", nullable = false, defaultValue = "true")
        format: Boolean = true,
        @Schema(description = "Parameter permission of video transcoding", nullable = false, implementation = Parameters::class)
        parameters: Parameters = Parameters(true, true, true, true)) {

        class Parameters(
            @Schema(description = "Whether transcoding resolution is allowed", nullable = false, defaultValue = "true")
            resolution: Boolean = true,
            @Schema(description = "Whether transcoding framerate is allowed", nullable = false, defaultValue = "true")
            framerate: Boolean = true,
            @Schema(description = "Whether transcoding bitrate is allowed", nullable = false, defaultValue = "true")
            bitrate: Boolean = true,
            @Schema(description = "Whether transcoding keyFrameInterval is allowed", nullable = false, defaultValue = "true")
            keyFrameInterval: Boolean = true) {
        }
    }
}
