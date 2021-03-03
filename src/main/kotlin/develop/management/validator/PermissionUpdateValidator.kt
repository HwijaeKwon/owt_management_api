package develop.management.validator

import develop.management.domain.dto.PermissionUpdate
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class PermissionUpdateValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return PermissionUpdate::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val permissionUpdate: PermissionUpdate = target as PermissionUpdate

        if(permissionUpdate.op != "replace") errors.rejectValue("op", "field.invalid", "Invalid op.")

        when(permissionUpdate.path) {
            "/permission/publish/audio" -> {}
            "/permission/publish/video" -> {}
            "/permission/subscribe/audio" -> {}
            "/permission/subscribe/video" -> {}
            else -> errors.rejectValue("path", "field.invalid", "Invalid path.")
        }
    }
}