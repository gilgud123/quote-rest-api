package com.katya.quoterestapi.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

/** Standard error response for API errors. */
@Schema(description = "Error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @Schema(description = "Timestamp when the error occurred") LocalDateTime timestamp,
    @Schema(description = "HTTP status code") int status,
    @Schema(description = "HTTP status reason phrase") String error,
    @Schema(description = "Error message") String message,
    @Schema(description = "Request path that caused the error") String path,
    @Schema(description = "Validation errors (field -> error message)")
        Map<String, String> validationErrors) {

  /** Constructor for simple error response without validation errors */
  public ErrorResponse(
      LocalDateTime timestamp, int status, String error, String message, String path) {
    this(timestamp, status, error, message, path, null);
  }
}
