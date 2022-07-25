package com.kakao.pretest.search.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.Map;

@RestControllerAdvice(basePackageClasses = LocalSearchController.class)
public class LocalSearchControllerAdvisor {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(ConstraintViolationException e) {
        var error = makeErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> paramExceptionHandler(MissingServletRequestParameterException e) {
        var error = makeErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String, Object>> commonExceptionHandler(Exception e) {
        var error = makeErrorMessage(HttpStatus.SERVICE_UNAVAILABLE.value(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .body(error);
    }

    private Map<String,Object> makeErrorMessage(int status, String message) {
        return Map.of("status", status, "message", message);
    }

}
