package develop.management.domain

/**
 * region object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class Region(val id: String, val shape: String, val area: Area) {

    data class Area(val left: Number, //the left corner ratio of the region, [0, 1]
               val top: Number, //the top corner ratio of the region, [0, 1]
               val width: Number, //the width ratio of the region, [0, 1]
               val height: Number) { //the height ratio of the region, [0, 1]
    }
}