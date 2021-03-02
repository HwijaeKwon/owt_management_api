package develop.management.util.error

/**
 * 인증 관련 error를 처리하는 클래스
 */
class AuthenticationError(message: String) : AppError(message) {
    override var code = 1101
    override var status = 401

    init {
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }

    constructor(message: String, code: Int) : this(message) {
        this.message = message
        this.code = code
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }
}