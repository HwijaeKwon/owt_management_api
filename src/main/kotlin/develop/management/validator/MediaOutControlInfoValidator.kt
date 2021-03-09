package develop.management.validator

import develop.management.domain.dto.MediaOutControlInfo
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class MediaOutControlInfoValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return MediaOutControlInfo::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val mediaOutControlInfo: MediaOutControlInfo = target as MediaOutControlInfo

        if(mediaOutControlInfo.op != "replace")
            errors.rejectValue("op", "field.invalid", "Invalid op.")
        when(mediaOutControlInfo.path) {
            "/output/media/video/parameters/from" -> {}
            "/output/media/video/parameters/resolution" -> {}
            "/output/media/video/parameters/framerate" -> {}
            "/output/media/video/parameters/bitrate" -> {}
            "/output/media/video/parameters/keyFrameInterval" -> {}
            "/output/media/audio/parameters/from" -> {}
            "/output/media/audio/parameters/resolution" -> {}
            "/output/media/audio/parameters/framerate" -> {}
            "/output/media/audio/parameters/bitrate" -> {}
            "/output/media/audio/parameters/keyFrameInterval" -> {}
            else -> errors.rejectValue("path", "field.invalid", "Invalid path.")
        }
    }
}