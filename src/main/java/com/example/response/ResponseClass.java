package com.example.response;

import org.springframework.http.HttpStatus;

/**
 * Generic response wrapper for API responses.
 *
 * @param <T> Type of the response entity
 */
public class ResponseClass<T> {
    
    private HttpStatus status;
    private String message;
    private T responseEntity;

    /**
     * Default constructor.
     */
    public ResponseClass() {}

    /**
     * Constructs a response with status, message, and entity.
     *
     * @param status HTTP status
     * @param message Response message
     * @param responseEntity Response payload
     */
    public ResponseClass(HttpStatus status, String message, T responseEntity) {
        this.status = status;
        this.message = message;
        this.responseEntity = responseEntity;
    }

    /**
     * Gets the HTTP status.
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Sets the HTTP status.
     *
     * @param status HTTP status
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * Gets the response message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the response message.
     *
     * @param message Response message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the response entity.
     */
    public T getResponseEntity() {
        return responseEntity;
    }

    /**
     * Sets the response entity.
     *
     * @param responseEntity Response payload
     */
    public void setResponseEntity(T responseEntity) {
        this.responseEntity = responseEntity;
    }
}
