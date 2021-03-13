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
            errors.rejectValue("connection.url", "field.invalid", "Invalid url.")
        if(connection.transportProtocol != "tcp" && connection.transportProtocol != "udp")
            errors.rejectValue("connection.transportProtocol", "field.invalid", "Invalid transportProtocol.")
        if(connection.bufferSize < 0)
            errors.rejectValue("connection.bufferSize", "field.invalid", "Invalid bufferSize.")
        if(media.audio != "auto" && media.audio != true && media.audio != false)
            errors.rejectValue("media.audio", "field.invalid", "Invalid audio.")
        if(media.video != "auto" && media.video != true && media.video != false)
            errors.rejectValue("media.video", "field.invalid", "Invalid video.")
    }
}