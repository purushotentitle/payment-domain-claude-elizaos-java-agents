package com.example.payments.api;

import com.example.payments.agents.UnknownAgentException;
import com.example.payments.approvals.ApprovalNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnknownAgentException.class)
    ResponseEntity<Map<String, Object>> unknownAgent(UnknownAgentException ex) {
        return error(HttpStatus.NOT_FOUND, "unknown_agent", ex.getMessage());
    }

    @ExceptionHandler(ApprovalNotFoundException.class)
    ResponseEntity<Map<String, Object>> approvalNotFound(ApprovalNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "approval_not_found", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validationError(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation_failed");
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unhandled(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
