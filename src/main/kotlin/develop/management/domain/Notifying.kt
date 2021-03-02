package develop.management.domain

/**
 * notifying object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
class Notifying(participantActivities: Boolean, //whether enable notification for participantActivities
                streamChange: Boolean) { //whether enable notification for streamChange
    val participantActivities = true
    val streamChange = true
}
