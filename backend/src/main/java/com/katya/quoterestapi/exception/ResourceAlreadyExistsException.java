package com.katya.quoterestapi.exception;

/** Exception thrown when there is a conflict with an existing resource. */
public class ResourceAlreadyExistsException extends RuntimeException {

  public ResourceAlreadyExistsException(String message) {
    super(message);
  }

  public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
    super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
  }
}
