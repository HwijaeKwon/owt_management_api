package develop.management.domain

/**
 * audio format object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class AudioFormat(val codec: String, //"opus", "pcmu", "pcma", "aac", "ac3", "nellymoser"
                       val sampleRate: Number?, //"opus/48000/2", "isac/16000/2", "isac/32000/2", "g722/16000/1"
                       val channelNum: Number?) { ////E.g "opus/48000/2", "opus" is codec, 48000 is sampleRate, 2 is channelNum
}