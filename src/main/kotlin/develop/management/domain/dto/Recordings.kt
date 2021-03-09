package develop.management.domain.dto

import develop.management.domain.OutMedia
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Client에게 recordings 정보를 전달하기 위한 dto 클래스
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 recordings 부분 참고
 */
data class Recordings(
        @Schema(description = "Unique identifier of the recording", nullable = false)
        val id: String,
        @Schema(description = "Out media of the recordings", nullable = false, implementation = OutMedia::class)
        val media: OutMedia,
        @Schema(description = "Storage of the recordings", nullable = false)
        val storage: Storage) {

        data class Storage(
                @Schema(description = "Host of the storage", nullable = false)
                val host: String,
                @Schema(description = "File name of the storage", nullable = false)
                val file: String)
}
