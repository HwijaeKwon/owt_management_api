package develop.management.validator

import develop.management.domain.dto.RoomConfig
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class RoomConfigValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return RoomConfig::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val roomConfig: RoomConfig = target as RoomConfig

        if(roomConfig.name.isBlank()) errors.rejectValue("name", "field.empty", "The name must not be empty.")
        if(roomConfig.options.participantLimit.toLong() < -1L) errors.rejectValue("options.participantLimit", "field.invalid", "The participantLimit is invalid.")
    }
}