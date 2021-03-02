package develop.management.domain

/**
 * room의 audio view object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class ViewAudio(val format: AudioFormat, //audio format
                val vad: Boolean) { //whether enable Voice Activity Detection for mixed audio
}