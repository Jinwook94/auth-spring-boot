package org.core.exception.general

import org.core.exception.ErrorType
import org.core.exception.base.InternalServerException

class GrpcException(
	errorType: ErrorType,
	invalidInput: String? = null,
	cause: Throwable? = null,
	additionalInfo: Map<String, Any?>? = null
) : InternalServerException(errorType, invalidInput, cause, additionalInfo)
