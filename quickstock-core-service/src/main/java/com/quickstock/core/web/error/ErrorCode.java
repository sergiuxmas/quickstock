package com.quickstock.core.web.error;

public enum ErrorCode {
	// generic request/400 family
	INVALID_REQUEST("INVALID_REQUEST", "The request is invalid."),
	MALFORMED_JSON("MALFORMED_JSON", "The request body contains malformed JSON."),
	VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed."),
	MISSING_REQUIRED_FIELD("MISSING_REQUIRED_FIELD", "A required field is missing."),
	INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", "Email must be a well-formed address."),
	INVALID_QUERY_PARAMETER("INVALID_QUERY_PARAMETER", "One or more query parameters are invalid."),
	INVALID_PAGINATION("INVALID_PAGINATION", "Pagination parameters are invalid."),
	INVALID_PRICE_RANGE("INVALID_PRICE_RANGE", "Price range parameters are invalid."),
	INVALID_CURRENCY("INVALID_CURRENCY", "Currency must be a 3-letter code."),

	// auth/security
	INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Email or password is invalid."),
	UNAUTHORIZED("UNAUTHORIZED", "Authentication is required."),
	FORBIDDEN("FORBIDDEN", "You do not have permission to perform this action."),
	METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "The HTTP method is not supported for this endpoint."),

	// generic API/runtime
	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "The requested resource was not found."),
	CONFLICT("CONFLICT", "The request conflicts with the current resource state."),
	INTERNAL_ERROR("INTERNAL_ERROR", "An unexpected internal error occurred.");

	private final String code;
	private final String defaultMessage;

	ErrorCode(String code, String defaultMessage) {
		this.code = code;
		this.defaultMessage = defaultMessage;
	}

	public String code() {
		return code;
	}

	public String defaultMessage() {
		return defaultMessage;
	}

	@Override
	public String toString() {
		return code;
	}
}
