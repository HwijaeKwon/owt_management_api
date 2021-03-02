package develop.management.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * room 생성 요청시 client가 전달하는 데이터
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * name, roomOptions -> required
 */
data class RoomConfig(
    @Schema(description = "Name of the room", nullable = false, minLength = 1, required = true, example = "name")
    val name: String,
    @Schema(description = "Option of the room", nullable = false, required = true, implementation = CreateOptions::class)
    val options: CreateOptions) {
}
