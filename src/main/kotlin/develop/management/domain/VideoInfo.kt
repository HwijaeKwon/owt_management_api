package develop.management.domain

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class VideoInfo(
        @Schema(description = "Video status (\"active\" or \"inactive\")", nullable = false, example = "active")
        val status: String,
        @Schema(description = "Video source (\"camera\", \"screen-case\", \"raw-file\", \"encoded-file\" or \"streaming\" for forward stream)", nullable = false, example = "camera")
        val source: String,
        @ArraySchema(schema = Schema(description = "Original video information", nullable = false, implementation = Original::class))
        val original: List<Original>) {
}

data class Original(
        @Schema(description = "Video format", nullable = false, implementation = VideoFormat::class)
        val format: VideoFormat,
        @Schema(description = "Video parameters", nullable = false, implementation = ViewVideo.Parameters::class)
        val parameters: ViewVideo.Parameters,
        @Schema(description = "Simulcast rid", required = false)
        val simulcastRid: String,
        @Schema(description = "Optional info (Not available for simulcast streams", required = false, implementation = OptionalVideo::class)
        val optional: OptionalVideo) {
}

data class OptionalVideo(
        @Schema(description = "Video format", required = false, implementation = VideoFormat::class)
        val format: VideoFormat,
        @Schema(description = "Video parameters", nullable = false, implementation = ViewVideo.Parameters::class)
        val parameters: ViewVideo.Parameters) {
}
