package develop.management.util.error

/**
 * Bad request 관련 error를 처리하는 클래스
 */
class BadRequestError(message: String) : AppError(message) {
    override var code = 1201
    override var status = 400

    companion object {
        const val example =
                """{
                    "code": 1201,
                    "message": "Invalid request body: Required arguments must not be null"
                    }"""
    }

    init {
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }

    constructor(message: String, code: Int) : this(message) {
        this.message = message
        this.code = code
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }
}