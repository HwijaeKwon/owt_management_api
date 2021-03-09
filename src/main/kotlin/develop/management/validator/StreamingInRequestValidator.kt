package develop.management.validator

import develop.management.domain.dto.StreamingInRequest
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class StreamingInRequestValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return StreamingInRequest::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val streamingInRequest: StreamingInRequest = target as StreamingInRequest
        val connection = streamingInRequest.connection
        val media = streamingInRequest.media

        if(connection.url.isBlank())
            errors.rejectValue("url", "field.invalid", "Invalid url.")
        if(connection.transportProtocol != "tcp" && connection.transportProtocol != "udp")
            errors.rejectValue("transportProtocol", "field.invalid", "Invalid transportProtocol.")
        if(connection.bufferSize < 0)
            errors.rejectValue("bufferSize", "field.invalid", "Invalid bufferSize.")
        if(media.audio != "auto" && media.audio != true && media.audio != false)
            errors.rejectValue("audio", "field.invalid", "Invalid audio.")
        if(media.video != "auto" && media.audio != true && media.audio != false)
            errors.rejectValue("video", "field.invalid", "Invalid video.")
    }
}