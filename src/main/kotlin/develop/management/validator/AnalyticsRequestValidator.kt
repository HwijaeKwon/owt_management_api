package develop.management.validator

import develop.management.domain.dto.AnalyticsRequest
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class AnalyticsRequestValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return AnalyticsRequest::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val analyticsRequest: AnalyticsRequest = target as AnalyticsRequest

        if(analyticsRequest.algorithm.isBlank())
            errors.rejectValue("algorithm", "field.invalid", "Invalid algorithm.")
    }
}