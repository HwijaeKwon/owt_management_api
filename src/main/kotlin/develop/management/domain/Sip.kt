package develop.management.domain

/**
 * sip object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class Sip(val sipServer: String?, //host or IP address for the SIP server
          val username: String?, //username of SIP account
          val password: String?) { //password of SIP account

    //Todo: 암호화, 복호화하는 부분이 필요하다
}
