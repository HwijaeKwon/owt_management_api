package develop.management.domain

import com.google.gson.Gson
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

    init {
        //Any type의 video를 적절한 object로 변환한다
        if(this.video !is ViewVideo && this.video != false) {
            val viewVideo = Gson().fromJson(this.video.toString(), ViewVideo::class.java)
            this.video = viewVideo
        }
    }
}

