package develop.management.domain

/**
 * video format object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class VideoFormat(val codec: String, //"h264", "vp8", "h265", "vp9"
                  val profile: String?) { //For "h264" output only, CB", "B", "M", "H"
}
