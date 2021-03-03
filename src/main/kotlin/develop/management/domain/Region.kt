package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * region object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class Region(
    @Schema(description = "Unique identifier of region")
    val id: String,
    @Schema(description = "Shape of the region")
    val shape: String,
    @Schema(description = "Area of the region", implementation = Area::class)
    val area: Area) {

    data class Area(
        @Schema(description = "The left corner ratio of the region", minimum = "0", maximum = "1")
        val left: Number,
        @Schema(description = "The top corner ratio of the region", minimum = "0", maximum = "1")
        val top: Number,
        @Schema(description = "The width ratio of the region", minimum = "0", maximum = "1")
        val width: Number,
        @Schema(description = "The height ratio of the region", minimum = "0", maximum = "1")
        val height: Number) {
    }
}