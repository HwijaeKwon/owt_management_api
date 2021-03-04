package develop.management.domain.dto

import develop.management.domain.Region
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class MixedInfo(
        @Schema(description = "View label of mixed stream")
        val label: String,
        @ArraySchema(schema = Schema(description = "Layout of the stream", implementation = Layout::class))
        val layout: List<Layout>)

data class Layout(
        @Schema(description = "Unique identifier of the stream")
        val stream: String,
        @Schema(description = "Region of the stream")
        val region: Region)