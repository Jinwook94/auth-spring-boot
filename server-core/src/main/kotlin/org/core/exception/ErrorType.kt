package org.core.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*

enum class ErrorType(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    // COMMON
    ILLEGAL_ARGUMENT(BAD_REQUEST, "A001", "잘못된 파라미터입니다."),
    INVALID_APPROACH(BAD_REQUEST, "A002", "잘못된 접근입니다."),
    INVALID_REQUEST(BAD_REQUEST, "A003", "잘못된 요청입니다."),

    EXPIRED_ACCESS_TOKEN(UNAUTHORIZED, "T001", "만료된 access token"),

    INTERNAL_SERVER_ERR(INTERNAL_SERVER_ERROR, "9999", "예기치 못한 서버오류")
    ;

    fun getMessage(invalidInput: String? = null): String {
        return if (invalidInput.isNullOrEmpty()) message else "$message : $invalidInput"
    }
}
