package develop.management.validator

import develop.management.domain.DashParameters
import develop.management.domain.HlsParameters
import develop.management.domain.dto.StreamingOutRequest
import develop.management.domain.dto.SubscriptionControlInfo
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class SubscriptionControlInfoValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return SubscriptionControlInfo::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val subscriptionControlInfo: SubscriptionControlInfo = target as SubscriptionControlInfo

        if(subscriptionControlInfo.op != "replace")
            errors.rejectValue("op", "field.invalid", "Invalid op.")
        when(subscriptionControlInfo.path) {
            "/media/audio/from" -> {}
            "/media/video/from" -> {}
            "/media/video/parameters/resolution" -> {}
            "/media/video/parameters/bitrate" -> {}
            "/media/video/parameters/keyFrameInterval" -> {}
            else -> errors.rejectValue("path", "field.invalid", "Invalid path.")
        }
    }
}