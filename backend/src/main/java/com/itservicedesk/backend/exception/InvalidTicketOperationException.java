package com.itservicedesk.backend.exception;

public class InvalidTicketOperationException extends RuntimeException {

    public InvalidTicketOperationException(String message) {
        super(message);
    }
}
