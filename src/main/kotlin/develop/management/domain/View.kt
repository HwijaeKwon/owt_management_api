package develop.management.domain

import com.google.gson.Gson

/**
 * room의 view object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
class View(val label: String,
           val audio: ViewAudio, //audio setting for the view
           var video: Any) { //video setting for the view (ViewVideo | false)

    init {
        //Any type의 video를 적절한 object로 변환한다
        if(this.video !is ViewVideo && this.video != false) {
            val viewVideo = Gson().fromJson(this.video.toString(), ViewVideo::class.java)
            this.video = viewVideo
        }
    }
}

