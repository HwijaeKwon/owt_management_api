package develop.management.validator

import develop.management.domain.dto.RecordingRequest
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class RecordingRequestValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return RecordingRequest::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val recordingRequest: RecordingRequest = target as RecordingRequest

        if(recordingRequest.container != "auto" && recordingRequest.container != "mkv" && recordingRequest.container != "mp4")
            errors.rejectValue("container", "field.invalid", "Invalid container.")
    }
}