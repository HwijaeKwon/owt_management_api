package develop.management.domain

import io.swagger.v3.oas.annotations.media.Schema

/**
 * room의 role object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class Role(
    @Schema(description = "Name of the role")
    val role: String,
    @Schema(description = "Role of publication", implementation = Publish::class)
    val publish: Publish,
    @Schema(description = "Role of subscription", implementation = Subscribe::class)
    val subscribe: Subscribe) {

    data class Publish(
        @Schema(description = "Whether the role can publish video")
        val video: Boolean,
        @Schema(description = "Whether the role can publish audio")
        val audio: Boolean) {
    }

    data class Subscribe(
        @Schema(description = "Whether the role can subscribe video")
        val video: Boolean,
        @Schema(description = "Whether the role can subscribe audio")
        val audio: Boolean) {
    }
}

