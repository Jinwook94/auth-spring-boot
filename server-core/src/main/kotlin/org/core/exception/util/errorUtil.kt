package org.core.exception.util

import org.core.exception.ErrorType
import org.springframework.http.ProblemDetail
import java.net.URI

/**
 * ErrorType 을 기반으로 ProblemDetail 생성하는 확장 함수
 */
fun ErrorType.toProblem(
	detail: String? = null,
	instance: URI? = null,
	additionalInfo: Map<String, Any?>? = null
): ProblemDetail {
	val problemDetail = ProblemDetail.forStatus(this.status)
	problemDetail.title = this.message
	problemDetail.type = URI.create("urn:core:errors:$code")
	problemDetail.setProperty("errorCode", this.code)

	detail?.let { problemDetail.detail = it }
	instance?.let { problemDetail.instance = it }
	additionalInfo?.forEach { (key, value) ->
		problemDetail.setProperty(key, value)
	}

	return problemDetail
}
