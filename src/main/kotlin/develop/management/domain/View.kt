package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * room의 view object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
class View(
    @Schema(description = "Label of the view", nullable = false, required = true)
    val label: String,
    @Schema(description = "Audio setting for the view", nullable = false, required = true, implementation = ViewAudio::class)
    val audio: ViewAudio,
    @Schema(description = "Video setting for the view (ViewVideo | false)", nullable = false, required = true, implementation = ViewVideo::class)
    var video: Any) {
}

