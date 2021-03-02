package develop.management.util.error

/**
 * NotFound 관련 error를 처리하는 클래스
 *
 * Internal Error Code
 * General not found - 1001
 * Service not found - 1002
 * Room not found - 1003
 * Stream not found - 1004
 * Participant not found - 1005
 *
 */
class NotFoundError(message: String) : AppError(message) {
    override var code = 1001
    override var status = 404

    companion object {
        const val exampleService =
                """{
                    "code": 1002,
                    "message": "Service not found"
                    }"""
        const val exampleRoom =
                """{
                    "code": 1003,
                    "message": "Room not found"
                    }"""
        const val exampleStream =
                """{
                    "code": 1004,
                    "message": "Stream not found"
                    }"""
        const val exampleParticipant =
                """{
                    "code": 1005,
                    "message": "Participant not found"
                    }"""
    }

    init {
        when {
            this.message.toLowerCase().contains("service") ->  this.code = 1002
            this.message.toLowerCase().contains("room") -> this.code = 1003
            this.message.toLowerCase().contains("stream") -> this.code = 1004
            this.message.toLowerCase().contains("participant") -> this.code = 1005
            else -> this.code = 1001
        }
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }

    constructor(message: String, code: Int) : this(message) {
        this.message = message
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }
}