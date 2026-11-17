package com.globaltrust.bank.web;

import com.globaltrust.bank.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/** Turns domain and security exceptions into clean JSON responses with the right HTTP status. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", error,
                "message", message));
    }

    @ExceptionHandler(SoDViolationException.class)
    public ResponseEntity<Map<String, Object>> sod(SoDViolationException ex) {
        return body(HttpStatus.FORBIDDEN, "Separation of Duty Violation", ex.getMessage());
    }

    @ExceptionHandler(WallViolationException.class)
    public ResponseEntity<Map<String, Object>> wall(WallViolationException ex) {
        return body(HttpStatus.FORBIDDEN, "Chinese Wall Violation", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> denied(AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "Access Denied", ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> conflict(ConflictException ex) {
        return body(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> bad(BadRequestException ex) {
        return body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .findFirst().orElse("Validation failed");
        return body(HttpStatus.BAD_REQUEST, "Validation Failed", msg);
    }
}
