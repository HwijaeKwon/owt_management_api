package develop.management.domain

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

/**
 * mediaIn object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class MediaIn(
    @ArraySchema(schema = Schema(description = "Audio format of media in", nullable = false, required = true, implementation = AudioFormat::class))
    val audio: List<AudioFormat>,
    @ArraySchema(schema = Schema(description = "Video format of media in", nullable = false, required = true, implementation = VideoFormat::class))
    val video: List<VideoFormat>) {
}
