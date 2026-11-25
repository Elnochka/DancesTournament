package com.dances.exception;

import com.dances.entity.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

//    @Test
//    void handleNotFound_shouldReturn404() {
//        NotFoundException exception =
//                new NotFoundException("Category not found");
//
//        ResponseEntity<Map<String, Object>> response =
//                handler.handleNotFound(exception);
//
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//
//        assertNotNull(response.getBody());
//        assertEquals(404, response.getBody().get("status"));
//        assertEquals("Not Found", response.getBody().get("error"));
//        assertEquals("Category not found", response.getBody().get("message"));
//        assertNotNull(response.getBody().get("timestamp"));
//    }

    @Test
    void handleNotFound_shouldReturn404() {
        NotFoundException exception =
                new NotFoundException("Category not found");

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("Not Found", body.getError());
        assertEquals("Category not found", body.getMessage());
        assertNotNull(body.getTimestamp());
        assertTrue(body.getDetails().isEmpty());
    }

    @Test
    void handleBadRequest_shouldReturn400ForIllegalArgumentException() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Invalid category ID");

        ResponseEntity<ErrorResponse> response =
                handler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("Invalid category ID", body.getMessage());
        assertNotNull(body.getTimestamp());
        assertTrue(body.getDetails().isEmpty());
    }

    @Test
    void handleBadRequest_shouldReturn400ForIllegalStateException() {
        IllegalStateException exception =
                new IllegalStateException("Invalid application state");

        ResponseEntity<ErrorResponse> response =
                handler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("Invalid application state", body.getMessage());
        assertNotNull(body.getTimestamp());
        assertTrue(body.getDetails().isEmpty());
    }

    @Test
    void handleValidationErrors_shouldReturn400WithFieldErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(
                new FieldError("request", "name", "Name is required")
        );

        MethodParameter parameter = new MethodParameter(
                getClass().getDeclaredMethod("dummyMethod", String.class), 0
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationErrors(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Validation Failed", body.getError());
        assertEquals("Request validation failed", body.getMessage());
        assertNotNull(body.getTimestamp());

        assertEquals(1, body.getDetails().size());
        assertEquals("Name is required", body.getDetails().get("name"));
    }

    private void dummyMethod(String value) {
    }

    @Test
    void handleDuplicate_shouldReturn409() {
        DuplicateException exception =
                new DuplicateException("Category already exists");

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicate(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(409, body.getStatus());
        assertEquals("Conflict", body.getError());
        assertEquals("Category already exists", body.getMessage());
        assertNotNull(body.getTimestamp());
        assertTrue(body.getDetails().isEmpty());
    }

    @Test
    void handleServerError_shouldReturn500() {
        Exception exception =
                new RuntimeException("Database connection failed");

        ResponseEntity<ErrorResponse> response =
                handler.handleServerError(exception);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals("Internal server error", body.getMessage());
        assertNotNull(body.getTimestamp());
        assertTrue(body.getDetails().isEmpty());

        assertNotEquals(
                "Database connection failed",
                body.getMessage()
        );
    }

    @Test
    void handleValidationErrors_shouldKeepFirstMessageForDuplicateFieldErrors()
            throws NoSuchMethodException {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(
                new FieldError("request", "name", "First error")
        );
        bindingResult.addError(
                new FieldError("request", "name", "Second error")
        );

        MethodParameter parameter = new MethodParameter(
                getClass().getDeclaredMethod("dummyMethod", String.class), 0
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationErrors(exception);

        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(1, body.getDetails().size());
        assertEquals("First error", body.getDetails().get("name"));
    }

    @Test
    void handleValidationErrors_shouldUseDefaultMessageWhenErrorMessageIsNull()
            throws NoSuchMethodException {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(
                new FieldError("request", "name", null)
        );

        MethodParameter parameter = new MethodParameter(
                getClass().getDeclaredMethod("dummyMethod", String.class), 0
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationErrors(exception);

        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals("Invalid value", body.getDetails().get("name"));
    }

    @Test
    void handleAccessDenied_shouldReturn403() {
        AccessDeniedException exception =
                new AccessDeniedException("User does not have permission");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(403, body.getStatus());
        assertEquals("Forbidden", body.getError());
        assertEquals("Access denied", body.getMessage());
        assertNotNull(body.getTimestamp());
        assertTrue(body.getDetails().isEmpty());
    }

}
