package com.moodplaylist.presentation;

import com.moodplaylist.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

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
}
