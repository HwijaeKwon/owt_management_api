package develop.management.util.error

/**
 * AppError를 client에게 전달하기 위한 response를 만드는 클래스
 */
open class AppError(open var message: String) {
    open var status: Int = 500
    open var code: Int = 2001
    open lateinit var errorBody: ErrorBody

    companion object {
        const val example =
                """{
                    "code": 2001,
                    "message": "Reason"
                    }"""
    }

    init {
        this.message = message
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }

    constructor(message:String, code: Int) : this(message) {
        this.code = code
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }

    constructor(message:String, code: Int, status: Int) : this(message, code) {
        this.status = status
        this.errorBody = ErrorBody(ErrorFoam(this.message, this.code))
    }
}

/**
 * Json 형식을 만들기 위해 사용하는 data class
 */
data class ErrorBody(var error: ErrorFoam) {}

data class ErrorFoam(var message: String, var code: Int) { }