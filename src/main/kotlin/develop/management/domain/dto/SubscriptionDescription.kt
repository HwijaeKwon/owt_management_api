package develop.management.domain.dto

import develop.management.domain.MediaSubOptions

data class SubscriptionDescription(
        var type: String = "",
        var connection: Connection = Connection(),
        var media: MediaSubOptions = MediaSubOptions(false, false)) {

        data class Connection(var algorithm: String = "")
}
