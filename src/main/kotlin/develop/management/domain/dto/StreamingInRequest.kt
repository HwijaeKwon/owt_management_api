package develop.management.domain.dto

import develop.management.domain.MediaInfo
import io.swagger.v3.oas.annotations.media.Schema
import java.sql.Connection

/**
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/의 Streaming-in 부분 참고
 */
data class StreamingInRequest(
        @Schema(description = "Connection of streaming in", nullable = false, required = true, implementation = Connection::class)
        val connection: Connection,
        @Schema(description = "Media of streaming in", nullable = false, required = true, implementation = Media::class)
        val media: Media,
        @Schema(description = "Type of streaming in", nullable = true, required = false)
        var type: String? = null) {

        data class Connection(
                @Schema(description = "Url of connection", nullable = false, required = true, example = "rtsp://...")
                val url: String,
                @Schema(description = "Transport protocol of connection", nullable = false, required = false, defaultValue = "tcp", example = "udp")
                val transportProtocol: String = "tcp",
                @Schema(description = "The buffer size in bytes in case \"udp\" is specified, 8182 by default", nullable = false, required = false, defaultValue = "8182")
                val bufferSize: Number = 8182)

        data class Media(
                @Schema(description = "Audio activation mode. \"auto\" | \"true\" | \"false\"", nullable = false, required = true, example = "auto")
                val audio: Any,
                @Schema(description = "Video activation mode. \"auto\" | \"true\" | \"false\"", nullable = false, required = true, example = "auto")
                val video: Any)
}
