package develop.management.domain

/**
 * transcoding object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
class Transcoding(audio: Boolean,
                  video: Video) {
    val audio = true //if allow transcoding format(opus, pcmu, ...) for audio
    val video = Video(true, Video.Parameters(true, true, true, true))

    class Video(format: Boolean,
                parameters: Parameters) {

        val format = true //if allow transcoding format(vp8, h264, ...) for video
        val parameters = Parameters(true, true, true, true)

        class Parameters(resolution: Boolean,
                         framerate: Boolean,
                         bitrate: Boolean,
                         keyFrameInterval: Boolean) {
            val resolution = true //if allow transcoding resolution for video
            val framerate = true //if allow transcoding framerate for video
            val bitrate = true //if allow transcoding bitrate for video
            val keyFrameInterval  = true //if allow transcoding KFI for video
        }
    }
}
