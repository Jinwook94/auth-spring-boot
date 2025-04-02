package org.core.common

open class CustomResponse(
    var code: String = "0000",
    var message: String = "성공",
    var additionalInfo: Map<String, Any?>? = null,
) {

    companion object {
        fun ok(): CustomResponse {
            return CustomResponse()
        }

        fun ok(code: String, message: String): CustomResponse {
            return CustomResponse(code, message)
        }

        fun error(code: String, message: String, additionalInfo: Map<String, Any?>? = null): CustomResponse {
            return CustomResponse(code, message, additionalInfo)
        }

        fun ok(body: Any?): Body<Any?> {
            return Body(body)
        }

        fun <T> ok(code: String, message: String, body: T): Body<T> {
            return Body(code, message, body)
        }
    }

    class Body<T>(
        val result: T
    ) : CustomResponse() {
        constructor(code: String, message: String, result: T) : this(result) {
            this.code = code
            this.message = message
        }
    }
}
