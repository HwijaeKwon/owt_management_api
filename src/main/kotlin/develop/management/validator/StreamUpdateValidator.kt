package develop.management.validator

import develop.management.domain.dto.StreamUpdate
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class StreamUpdateValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return StreamUpdate::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {

        val streamUpdate: StreamUpdate = target as StreamUpdate

        if(streamUpdate.op != "replace"
            && streamUpdate.op != "add"
            && streamUpdate.op != "remove")
                errors.rejectValue("op", "field.empty", "Invalid op.")
    }
}