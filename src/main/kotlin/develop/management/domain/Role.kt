package develop.management.domain

/**
 * room의 role object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class Role(val role: String, //name of the role
           val publish: Publish,
           val subscribe: Subscribe) {

    //Todo: publish와 subscribe가 object가 아니라 map이어야하나?
    data class Publish(val video: Boolean, //whether the role can publish video
                  val audio: Boolean) { //whether the role can publish audio
    }

    data class Subscribe(val video: Boolean, //whether the role can subscribe video
                    val audio: Boolean) { //whether the role can subscribe audio
    }
}

