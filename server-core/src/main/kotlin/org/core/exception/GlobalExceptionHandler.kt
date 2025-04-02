package org.core.exception

import jakarta.validation.ConstraintViolationException
import org.core.exception.ErrorType.*
import org.core.exception.base.BadRequestException
import org.core.exception.base.ForbiddenException
import org.core.exception.base.UnauthorizedException
import org.core.exception.dto.ValidationError
import org.core.exception.util.toProblem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
import org.slf4j.event.Level.ERROR
import org.slf4j.event.Level.INFO
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BadRequestException::class)
    fun badRequestExceptionHandler(e: BadRequestException): ResponseEntity<ProblemDetail> {
        val errorType = e.getErrorType()
        val additionalInfo = e.getAdditionalInfo()
        val problemDetail = handleExceptionWithProblem(
            e,
            errorType,
            e.message,
            ERROR,
            additionalInfo
        )
        return ResponseEntity.status(errorType.status).body(problemDetail)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun forBiddenExceptionHandler(e: ForbiddenException): ResponseEntity<ProblemDetail> {
        val errorType = e.getErrorType()
        val problemDetail = handleExceptionWithProblem(
            e,
            errorType,
            e.message,
            ERROR
        )
        return ResponseEntity.status(errorType.status).body(problemDetail)
    }


    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorizedExceptionHandler(e: UnauthorizedException): ResponseEntity<ProblemDetail> {
        val errorType = e.getErrorType()
        val loggingLevel = if (errorType == EXPIRED_ACCESS_TOKEN) INFO else ERROR
        val problemDetail = handleExceptionWithProblem(
            e,
            errorType,
            e.message,
            loggingLevel
        )
        return ResponseEntity.status(errorType.status).body(problemDetail)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolationExceptionHandler(e: ConstraintViolationException): ResponseEntity<ProblemDetail> {
        val validationErrors = extractValidationErrors(e.constraintViolations)
        val problemDetail = handleExceptionWithProblem(
            e,
            INVALID_REQUEST,
            "요청 파라미터 유효성 검증 실패",
            ERROR,
            mapOf("violations" to validationErrors)
        )
        return ResponseEntity.status(INVALID_REQUEST.status).body(problemDetail)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentExceptionHandler(e: IllegalArgumentException): ResponseEntity<ProblemDetail> {
        val problemDetail = handleExceptionWithProblem(
            e,
            INVALID_REQUEST,
            e.message ?: "잘못된 요청입니다.",
            ERROR
        )
        return ResponseEntity.status(INVALID_REQUEST.status).body(problemDetail)
    }

    @ExceptionHandler(BindException::class)
    fun bindExceptionHandler(e: BindException): ResponseEntity<ProblemDetail> {
        val validationErrors = extractValidationErrors(e.bindingResult)
        val problemDetail = handleExceptionWithProblem(
            e,
            INVALID_REQUEST,
            "요청 본문 유효성 검증 실패",
            ERROR,
            mapOf("violations" to validationErrors)
        )
        return ResponseEntity.status(INVALID_REQUEST.status).body(problemDetail)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentExceptionHandler(e: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val validationErrors = extractValidationErrors(e.bindingResult)
        val problemDetail = handleExceptionWithProblem(
            e,
            INVALID_REQUEST,
            "요청 본문 유효성 검증 실패",
            ERROR,
            mapOf("violations" to validationErrors)
        )
        return ResponseEntity.status(INVALID_REQUEST.status).body(problemDetail)
    }

    @ExceptionHandler(
        Exception::class,
        IllegalStateException::class,
        RuntimeException::class,
        java.lang.IllegalStateException::class,
    )
    fun unCaughtException(e: Exception): ResponseEntity<ProblemDetail> {
        val problemDetail = handleExceptionWithProblem(
            e,
            INTERNAL_SERVER_ERR,
            e.message,
            ERROR
        )
        return ResponseEntity.status(INTERNAL_SERVER_ERR.status).body(problemDetail)
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun missingRequestHeaderException(e: MissingRequestHeaderException): ResponseEntity<ProblemDetail> {
        val problemDetail = handleExceptionWithProblem(
            e,
            INVALID_REQUEST,
            "필수 헤더값이 없습니다: ${e.headerName}",
            ERROR
        )
        return ResponseEntity.status(INVALID_REQUEST.status).body(problemDetail)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun missingRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ProblemDetail> {
        val problemDetail = handleExceptionWithProblem(
            e,
            INVALID_REQUEST,
            "필수 파라미터가 없습니다: ${e.parameterName}",
            ERROR
        )
        return ResponseEntity.status(INVALID_REQUEST.status).body(problemDetail)
    }

    private fun handleExceptionWithProblem(
        e: Exception,
        errorType: ErrorType,
        detailMessage: String? = null,
        loggingLevel: Level = ERROR,
        additionalInfo: Map<String, Any?>? = null
    ): ProblemDetail {
        // 로깅 처리
        logException(e, errorType.status, detailMessage, loggingLevel)

        // 현재 요청 URI 가져오기
        val requestURI = getCurrentRequestURI()

        // ProblemDetail 생성 및 반환
        return errorType.toProblem(
            detail = detailMessage,
            instance = requestURI,
            additionalInfo = additionalInfo
        )
    }

    private fun logException(e: Exception, status: HttpStatus, message: String? = null, level: Level = ERROR) {
        // MDC 사용해 HTTP 상태 코드 로깅
        MDC.put("httpStatus", status.value().toString())

        // 로그 메시지
        val messageToLog = message ?: e.message

        // 로깅 레벨에 따라 로깅
        when (level) {
            INFO -> logger.info(messageToLog, e)
            else -> logger.error(messageToLog, e)
        }

        // MDC 정보 제거
        MDC.remove("httpStatus")
    }

    private fun getCurrentRequestURI(): URI? {
        return try {
            val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
            URI.create(request.requestURI)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractValidationErrors(bindingResult: BindingResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // 필드 오류 처리
        bindingResult.fieldErrors.forEach { fieldError ->
            errors.add(
                ValidationError(
                    field = fieldError.field,
                    value = fieldError.rejectedValue,
                    reason = fieldError.defaultMessage ?: "유효하지 않은 값"
                )
            )
        }

        // 글로벌 오류 처리
        bindingResult.globalErrors.forEach { globalError ->
            errors.add(
                ValidationError(
                    field = globalError.objectName,
                    value = null,
                    reason = globalError.defaultMessage ?: "유효하지 않은 객체"
                )
            )
        }

        return errors
    }

    private fun extractValidationErrors(constraintViolations: Set<jakarta.validation.ConstraintViolation<*>>): List<ValidationError> {
        return constraintViolations.map { violation ->
            ValidationError(
                field = violation.propertyPath.toString(),
                value = violation.invalidValue,
                reason = violation.message
            )
        }
    }
}
