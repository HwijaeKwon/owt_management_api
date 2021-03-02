package develop.management.domain

/**
 * room의 video view object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class ViewVideo(val format: VideoFormat, //video format
                val parameters: Parameters,
                val maxInput: Number, //input limit for the view, positive integer. min: 1, max: 256
                val bgColor: BgColor,
                val motionFactor: Number, //float, affact the bitrate
                val keepActiveInputPrimary: Boolean, //keep active audio's related video in primary region in the view
                val layout: Layout) {

    class Parameters(val resolution: Resolution, //resolution
                     val framerate: Number, //valid values in [6, 12, 15, 24, 30, 48, 60]
                     val bitrate: Number?, //Kbps
                     val keyFrameInterval: Number) { //valid values in [100, 30, 5, 2, 1]

    }

    class BgColor(val r: Number, //0 ~ 255
                  val g: Number, //0 ~ 255
                  val b: Number) { //0 ~ 255
    }

    class Layout(val fitPolicy: String, //"letterbox" or "crop"
                 val setRegionEffect: String?,
                 val templates: Templates) {

        class Templates(val base: String, //template base, valid values ["fluid", "lecture", "void"]
                        val custom: List<List<Region>>) { // user customized layout applied on base
            //custom: a region list of length K represents a K-region-layout. detail of Region refer to the object(Region)
        }
    }
}