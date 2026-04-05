package com.moodplaylist.presentation;

import com.moodplaylist.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @SuppressWarnings("unused")
    private static class DummyRequest {
        private String moodText;
    }

    @Test
    void illegalState_withUnauthorizedMessage_returns401() {
        var response = handler.handleIllegalState(new IllegalStateException("unauthorized"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ApiResponse<Void> body = response.getBody();
        assertEquals(false, body.success());
        assertEquals("BAD_STATE", body.error().code());
        assertEquals("unauthorized", body.error().message());
    }

    @Test
    void illegalState_withOtherMessage_returns400() {
        var response = handler.handleIllegalState(new IllegalStateException("quota exhausted"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Void> body = response.getBody();
        assertEquals(false, body.success());
        assertEquals("BAD_STATE", body.error().code());
        assertEquals("quota exhausted", body.error().message());
    }

    @Test
    void illegalArgument_returns400BadRequestCode() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("mood log not found"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Void> body = response.getBody();
        assertEquals(false, body.success());
        assertEquals("BAD_REQUEST", body.error().code());
        assertEquals("mood log not found", body.error().message());
    }

    @Test
    void methodArgumentNotValid_returnsValidationError() throws Exception {
        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleMethod", String.class), 0
        );
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DummyRequest(), "dummyRequest");
        bindingResult.rejectValue("moodText", "NotBlank", "must not be blank");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        var response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Void> body = response.getBody();
        assertEquals(false, body.success());
        assertEquals("VALIDATION_ERROR", body.error().code());
        assertEquals("invalid request", body.error().message());
    }

    @Test
    void typeMismatch_returnsValidationError() {
        MethodArgumentTypeMismatchException mismatch = new MethodArgumentTypeMismatchException(
                "not-a-date", java.time.LocalDate.class, "date", null, new IllegalArgumentException("bad type")
        );

        var response = handler.handleTypeMismatch(mismatch);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Void> body = response.getBody();
        assertEquals(false, body.success());
        assertEquals("VALIDATION_ERROR", body.error().code());
        assertEquals("invalid request", body.error().message());
    }

    @SuppressWarnings("unused")
    private void sampleMethod(String moodText) {
        // test helper for MethodParameter
    }
}
