package develop.management.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * token 생성시 client에게 전달하는 데이터
 */
data class TokenInfo(
    @Schema(description = "Generated token", nullable = false)
    val token: String) {
    //필요한 데이터가 있다면 언제든지 추가할 수 있다
}