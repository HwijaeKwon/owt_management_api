package develop.management.domain.dto

import develop.management.domain.MediaSubOptions

data class StreamingOutsSubscriptionRequest(
        var type: String = "",
        var connection: Connection = Connection(),
        var media: MediaSubOptions = MediaSubOptions(false, false)) {

        data class Connection(var protocol: String = "", var url: String = "", var parameters: Any? = null)
}
