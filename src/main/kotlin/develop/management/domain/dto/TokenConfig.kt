package develop.management.domain.dto

import develop.management.domain.document.Token
import develop.management.domain.document.Token.Origin
import io.swagger.v3.oas.annotations.media.Schema

/**
 * token 요청시 client가 전달하는 데이터 클래스
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Token request 참고
 * user, role -> required
 */
data class TokenConfig(
    @Schema(description = "Unique identifier of the participant", nullable = false, required = true)
    val user: String,
    @Schema(description = "Role of the participant", nullable = false, required = true)
    val role: String,
    @Schema(description = "Origin of the token", nullable = false, required = true, implementation = Token.Origin::class)
    val preference: Token.Origin = Origin()) {
}