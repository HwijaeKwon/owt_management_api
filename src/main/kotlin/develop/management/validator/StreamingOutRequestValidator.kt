package develop.management.validator

import develop.management.domain.DashParameters
import develop.management.domain.HlsParameters
import develop.management.domain.dto.StreamingInRequest
import develop.management.domain.dto.StreamingOutRequest
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class StreamingOutRequestValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return StreamingOutRequest::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val streamingOutRequest: StreamingOutRequest = target as StreamingOutRequest

        if(streamingOutRequest.protocol != "rtmp" && streamingOutRequest.protocol != "rtsp" && streamingOutRequest.protocol != "hls" && streamingOutRequest.protocol != "dash")
            errors.rejectValue("protocol", "field.invalid", "Invalid protocol.")
        if(streamingOutRequest.url.isBlank())
            errors.rejectValue("url", "field.invalid", "Invalid url.")

        //Todo: parameters 형태 확인
        if(streamingOutRequest.parameters != null && (streamingOutRequest.parameters !is HlsParameters || streamingOutRequest.parameters !is DashParameters))
            errors.rejectValue("parameters", "field.invalid", "Invalid parameters.")
    }
}