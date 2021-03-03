package develop.management.validator

import develop.management.domain.dto.TokenConfig
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class TokenConfigValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return TokenConfig::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val tokenConfig: TokenConfig = target as TokenConfig

        if(tokenConfig.user.isBlank()) errors.rejectValue("user", "field.empty", "The user must not be empty.")

        if(tokenConfig.role.isBlank()) errors.rejectValue("role", "field.empty", "The role must not be empty.")
    }
}