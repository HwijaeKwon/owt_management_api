package develop.management.validator

import develop.management.domain.dto.ServiceConfig
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class ServiceConfigValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return ServiceConfig::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val serviceConfig: ServiceConfig = target as ServiceConfig

        if(serviceConfig.name.isBlank()) errors.rejectValue("name", "field.empty", "The name must not be empty.")

        if(serviceConfig.key.isBlank()) errors.rejectValue("key", "field.empty", "The key must not be empty.")
    }
}