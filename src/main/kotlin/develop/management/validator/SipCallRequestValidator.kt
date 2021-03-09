package develop.management.validator

import develop.management.domain.dto.SipCallRequest
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class SipCallRequestValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return SipCallRequest::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val sipCallRequest: SipCallRequest = target as SipCallRequest

        if(sipCallRequest.peerURI.isBlank())
            errors.rejectValue("peerURI", "field.invalid", "Invalid peerURI.")
    }
}