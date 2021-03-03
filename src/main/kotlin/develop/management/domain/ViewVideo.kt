package develop.management.domain

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

/**
 * room의 video view object
 * https://software.intel.com/sites/products/documentation/webrtc/restapi/ Rooms 참고
 * getter, setter 모두 열어뒀지만 room 객체 내부에서 private으로 정의하여 외부에서 접근하지 못 하게 한다
 */
data class ViewVideo(
    @Schema(description = "Video format")
    val format: VideoFormat,
    @Schema(description = "Video parameter", implementation = Parameters::class)
    val parameters: Parameters,
    @Schema(description = "Input limit for the view, positive integer", minimum = "1", maximum = "256")
    val maxInput: Number,
    @Schema(description = "Video bgcolor", implementation = BgColor::class)
    val bgColor: BgColor,
    @Schema(description = "Video motion factor. Float, affect the bitrate")
    val motionFactor: Number,
    @Schema(description = "Keep active audio's related video in primary region in the view")
    val keepActiveInputPrimary: Boolean,
    @Schema(description = "Video layout", implementation = Layout::class)
    val layout: Layout) {

    class Parameters(
        @Schema(description = "Video resolution", implementation = Resolution::class)
        val resolution: Resolution,
        @Schema(description = "Video framerate", example = "[6, 12, 15, 24, 30, 48, 60]")
        val framerate: Number,
        @Schema(description = "Video bitrate (Kbps)", nullable = true)
        val bitrate: Number?,
        @Schema(description = "Video key frame interval", example = "[100, 30, 5, 2, 1]")
        val keyFrameInterval: Number) {

    }

    class BgColor(
        @Schema(description = "R of bgColor", minimum = "0", maximum = "256")
        val r: Number,
        @Schema(description = "G of bgColor", minimum = "0", maximum = "256")
        val g: Number,
        @Schema(description = "B of bgColor", minimum = "0", maximum = "256")
        val b: Number) {
    }

    class Layout(
        @Schema(description = "Fit policy of layout", example = "letterbox or crop")
        val fitPolicy: String,
        @Schema(description = "Set region effect of layout", nullable = true)
        val setRegionEffect: String?,
        @Schema(description = "Templates of layout", implementation = Templates::class)
        val templates: Templates) {

        class Templates(
            @Schema(description = "Base of layout template", example = "[\"fluid\", \"lecture\", \"void\"]")
            val base: String,
            @ArraySchema(arraySchema = Schema(description = "User customized layout applied on base. " +
                    "A region list of length K represents a K-region-layout. detail of Region refer to the object(Region)", implementation = Region::class))
            val custom: List<List<Region>>) {
        }
    }
}