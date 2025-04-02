package org.core.exception

import lombok.AllArgsConstructor
import lombok.Getter

@Getter
@AllArgsConstructor
enum class ErrorType(val code: String, val message: String) {

    // COMMON
    ILLEGAL_ARGUMENT            ("A001", "잘못된 파라미터입니다."),
    INVALID_APPROACH            ("A002", "잘못된 접근입니다."),
    INVALID_REQUEST             ("A003", "잘못된 요청입니다."),
    EXPIRED_ACCESS_TOKEN        ("T001", "만료된 access token"),
    ;

    fun getMessage(invalidInput: String? = null): String {
        return if (invalidInput.isNullOrEmpty()) message else "$message : $invalidInput"
    }

}
