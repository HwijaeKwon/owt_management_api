package develop.management.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * service 생성시 client가 전달하는 데이터
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Services 참고
 * name, key -> required
 */
data class ServiceInfo(
    @Schema(description = "Unique identifier of the service", nullable = false)
    val _id: String,
    @Schema(description = "Name of the service", nullable = false, required = true)
    val name: String,
    @Schema(description = "Key of the service", nullable = false, required = true)
    val key: String) {
}