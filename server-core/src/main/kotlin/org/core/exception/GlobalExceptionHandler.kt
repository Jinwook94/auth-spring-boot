package org.core.exception

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.core.common.CustomResponse
import org.core.exception.ErrorType.*
import org.core.exception.base.BadRequestException
import org.core.exception.base.ForbiddenException
import org.core.exception.base.UnauthorizedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
import org.slf4j.event.Level.ERROR
import org.slf4j.event.Level.INFO
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BadRequestException::class)
    fun badRequestExceptionHandler(response: HttpServletResponse, e: BadRequestException): CustomResponse {
        val errorType = e.getErrorType()
        val additionalInfo = e.getAdditionalInfo()
        return handleException(response, e, BAD_REQUEST, e.message, errorType.code, errorType.message, ERROR, additionalInfo)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolationExceptionHandler(response: HttpServletResponse, e: ConstraintViolationException): CustomResponse {
        val errorMessages = e.constraintViolations.joinToString(separator = "; ") { violation ->
            "${violation.propertyPath}: ${violation.message}"
        }
        val detailedErrorMessage = "요청이 유효하지 않습니다. 오류 상세: $errorMessages"
        return handleException(response, e, BAD_REQUEST, e.message, INVALID_REQUEST.code, detailedErrorMessage)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentExceptionHandler(response: HttpServletResponse, e: IllegalArgumentException): CustomResponse {
        return handleException(response, e, BAD_REQUEST, e.message, INVALID_REQUEST.code, e.message ?: "잘못된 요청입니다.")
    }

    @ExceptionHandler(BindException::class)
    fun bindExceptionHandler(response: HttpServletResponse, e: BindException): CustomResponse {
        val detailedErrorMessage = createDetailedErrorMessage(e.bindingResult)
        return handleException(response, e, BAD_REQUEST, e.message, INVALID_REQUEST.code, detailedErrorMessage)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentExceptionHandler(response: HttpServletResponse, e: MethodArgumentNotValidException): CustomResponse {
        val detailedErrorMessage = createDetailedErrorMessage(e.bindingResult)
        return handleException(response, e, BAD_REQUEST, e.message, INVALID_REQUEST.code, detailedErrorMessage)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun forBiddenExceptionHandler(response: HttpServletResponse, e: ForbiddenException): CustomResponse {
        val errorType = e.getErrorType()
        return handleException(response, e, FORBIDDEN, e.message, errorType.code, errorType.message)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorizedExceptionHandler(response: HttpServletResponse, e: UnauthorizedException): CustomResponse {
        val errorType = e.getErrorType()
        val loggingLevel = if (errorType == EXPIRED_ACCESS_TOKEN) INFO else ERROR   // 액세스 토큰 만료는 INFO 레벨로
        return handleException(response, e, UNAUTHORIZED, e.message, errorType.code, errorType.message, loggingLevel)
    }

    @ExceptionHandler(
        Exception::class,
        IllegalStateException::class,
        RuntimeException::class,
        java.lang.IllegalStateException::class,
    )
    fun unCaughtException(response: HttpServletResponse, e: Exception): CustomResponse {
        return handleException(response, e, INTERNAL_SERVER_ERROR, e.message, "9999", "예기치 못한 서버오류: ${e.message}")
    }

    // 이미 존재하는 예외 처리들 ..
    @ExceptionHandler(MissingRequestHeaderException::class)
    fun missingRequestHeaderException(response: HttpServletResponse, e: MissingRequestHeaderException): CustomResponse {
        return handleException(response, e, BAD_REQUEST, e.message, INVALID_REQUEST.code, "필수 헤더값이 없습니다.")
    }


    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun missingRequestParameterException(response: HttpServletResponse, e: MissingServletRequestParameterException): CustomResponse {
        return handleException(response, e, BAD_REQUEST, e.message, INVALID_REQUEST.code, "필수 파라미터가 없습니다.")
    }

    private fun handleException(
        response: HttpServletResponse,
        e: Exception,
        status: HttpStatus,
        logMessage: String? = null,
        errorCode: String,
        errorMessage: String,
        loggingLevel: Level = ERROR,
        additionalInfo: Map<String, Any?>? = null,
    ): CustomResponse {
        // MDC 사용해 HTTP 상태 코드 로깅
        MDC.put("httpStatus", status.value().toString())

        // 커스텀 로그 메시지를 제공하거나, 예외의 메시지 사용
        val messageToLog = logMessage ?: e.message

        // 기본적으로 ERROR 레벨로 로깅. EXPIRED_ACCESS_TOKEN 에러 등 예외적으로 INFO 레벨 로깅
        when (loggingLevel) {
            INFO -> logger.info(messageToLog, e)
            else -> logger.error(messageToLog, e)
        }

        // MDC 에서 정보 제거
        MDC.remove("httpStatus")

        // 응답 상태 설정
        response.status = status.value()

        return CustomResponse.error(errorCode, errorMessage, additionalInfo)
    }

    private fun createDetailedErrorMessage(bindingResult: BindingResult): String {
        val fieldErrorMessages = bindingResult.fieldErrors.joinToString(separator = "; ") { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }

        val globalErrorMessages = bindingResult.globalErrors.joinToString(separator = "; ") { globalError ->
            "${globalError.objectName}: ${globalError.defaultMessage}"
        }

        return if (fieldErrorMessages.isNotBlank() || globalErrorMessages.isNotBlank()) {
            "요청이 유효하지 않습니다. 오류 상세: $fieldErrorMessages $globalErrorMessages"
        } else {
            "요청이 유효하지 않습니다."
        }
    }

}
