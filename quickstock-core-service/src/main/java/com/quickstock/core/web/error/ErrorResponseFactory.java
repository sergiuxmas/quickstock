package com.quickstock.core.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class ErrorResponseFactory {

	public ErrorResponse create(HttpStatus status, ErrorCode errorCode) {
		return create(status, errorCode, null, null);
	}

	public ErrorResponse create(HttpStatus status, ErrorCode errorCode, String message) {
		return create(status, errorCode, message, null);
	}

	public ErrorResponse create(HttpStatus status, ErrorCode errorCode, String message, String correlationId) {
		Objects.requireNonNull(status, "status must not be null");
		Objects.requireNonNull(errorCode, "errorCode must not be null");

		return new ErrorResponse(
				OffsetDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				errorCode.code(),
				resolveMessage(errorCode, message),
				correlationId
		);
	}

	private String resolveMessage(ErrorCode errorCode, String message) {
		if (message == null || message.isBlank()) {
			return errorCode.defaultMessage();
		}
		return message;
	}
}
