package develop.management.domain

/**
 * mediaOut object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class MediaOut(val audio: List<AudioFormat>, val video: Video) {

    data class Video(val format: List<VideoFormat>, val parameters: Parameters) {

        data class Parameters(val resolution: List<String>, //Array of resolution.E.g. ["x3/4", "x2/3", ... "cif"]
                         val framerate: List<Number>, //Array of framerate.E.g. [5, 15, 24, 30, 48, 60]
                         val bitrate: List<String>, //Array of bitrate.E.g. [500, 1000, ... ]
                         val keyFrameInterval: List<Number>) { //Array of keyFrameInterval.E.g. [100, 30, 5, 2, 1]
        }
    }
}
