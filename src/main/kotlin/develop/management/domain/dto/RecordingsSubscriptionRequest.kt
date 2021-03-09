package develop.management.domain.dto

import develop.management.domain.MediaSubOptions
import javax.swing.RootPaneContainer

data class RecordingsSubscriptionRequest(
        var type: String = "",
        var connection: Connection = Connection(),
        var media: MediaSubOptions = MediaSubOptions(false, false)) {

        data class Connection(var container: String = "")
}
