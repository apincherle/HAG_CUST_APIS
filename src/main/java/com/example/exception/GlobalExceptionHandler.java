package com.example.exception;

import com.example.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        System.err.println("DEBUG: Validation exception caught: " + ex.getMessage());
        System.err.println("DEBUG: Target: " + ex.getTarget());
        System.err.println("DEBUG: Binding result: " + ex.getBindingResult());
        
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            System.err.println("DEBUG: Validation error - Field: " + fieldName + ", Message: " + errorMessage);
            details.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String code = ex.getStatusCode().toString();
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            code = "NOT_FOUND";
        } else if (ex.getStatusCode() == HttpStatus.CONFLICT) {
            code = "CONFLICT";
        } else if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
            code = "BAD_REQUEST";
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .code(code)
                .message(ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString())
                .build();
        
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log the actual exception for debugging
        ex.printStackTrace();
        System.err.println("ERROR: " + ex.getClass().getName() + ": " + ex.getMessage());
        if (ex.getCause() != null) {
            System.err.println("CAUSE: " + ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage());
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("exception", ex.getClass().getName());
        details.put("message", ex.getMessage());
        if (ex.getCause() != null) {
            details.put("cause", ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage());
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred: " + ex.getMessage())
                .details(details)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

