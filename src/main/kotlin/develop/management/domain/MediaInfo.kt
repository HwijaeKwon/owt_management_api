package develop.management.domain

import com.google.gson.Gson
import io.swagger.v3.oas.annotations.media.Schema

data class MediaInfo(
        @Schema(description = "Audio information (\"AudioInfo\" or \"false\")", nullable = false, implementation = AudioInfo::class)
        var audio: Any,
        @Schema(description = "Video information (\"VideoInfo\" or \"false\")", nullable = false, implementation = VideoInfo::class)
        var video: Any) {

    init {
        //Any type의 audio를 적절한 object로 변환한다
        if(this.audio is AudioInfo && this.audio != false) {
            val audioInfo = Gson().fromJson(this.audio.toString(), AudioInfo::class.java)
            this.audio= audioInfo
        }

        //Any type의 video를 적절한 object로 변환한다
        if(this.video is VideoInfo && this.video != false) {
            val videoInfo = Gson().fromJson(this.video.toString(), VideoInfo::class.java)
            this.video = videoInfo
        }
    }
}
