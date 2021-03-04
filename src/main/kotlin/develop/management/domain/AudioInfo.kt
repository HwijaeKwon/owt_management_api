package develop.management.domain

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class AudioInfo(
        @Schema(description = "Audio status (\"active\" or \"inactive\")", nullable = false, example = "active")
        val status: String,
        @Schema(description = "Audio source (\"mic\", \"screen-cast\", \"raw-file\", \"encoded-file\" or \"streaming\" for forward stream)", nullable = false, example = "mic")
        val source: String,
        @Schema(description = "Audio format", nullable = false, implementation = AudioFormat::class)
        val format: AudioFormat,
        @Schema(description = "Audio optional format list", nullable = false, implementation = OptionalAudio::class)
        val optional: OptionalAudio) {
}

data class OptionalAudio(
        @ArraySchema(schema = Schema(description = "Optional format", implementation = AudioFormat::class))
        val format: List<AudioFormat>) {
}
