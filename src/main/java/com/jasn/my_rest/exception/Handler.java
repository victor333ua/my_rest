package com.jasn.my_rest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.Map;

// example of the exception handling in Spring
@ControllerAdvice
public final class Handler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(GifNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(GifNotFoundException ex) {
        return ResponseEntity
            .unprocessableEntity()
            .body(
                Map.of(
                    "error", ex.getMessage() == null ? "Unprocessable request parameters" : ex.getMessage()
                )
            );
    }

    @ExceptionHandler(MyIoException.class)
    public ResponseEntity<Object> handleIOException(MyIoException ex) {
 //       return ResponseEntity.notFound().build();
        return ResponseEntity
                .unprocessableEntity()
                .body(
                        Map.of(
                                "error", ex.getMessage() == null ? "Unprocessable request parameters" : ex.getMessage()
                        )
                );
    }
}
/*
@ControllerAdvice
public class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = { IllegalArgumentException.class, IllegalStateException.class })
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "This should be application specific";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }
}

 */