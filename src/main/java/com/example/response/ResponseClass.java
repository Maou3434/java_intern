package com.example.response;

// Spring HTTP status enum import
import org.springframework.http.HttpStatus;

/**
 * Generic wrapper for API responses.
 *
 * @param <T> Type of the response payload
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
     * Constructs a response with HTTP status, message, and payload.
     *
     * @param status        HTTP status code
     * @param message       Descriptive response message
     * @param responseEntity Response payload of generic type T
     */
    public ResponseClass(HttpStatus status, String message, T responseEntity) {
        this.status = status;
        this.message = message;
        this.responseEntity = responseEntity;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return HTTP status
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Sets the HTTP status code.
     *
     * @param status HTTP status
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * Returns the response message.
     *
     * @return response message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the response message.
     *
     * @param message response message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the response payload.
     *
     * @return response entity of type T
     */
    public T getResponseEntity() {
        return responseEntity;
    }

    /**
     * Sets the response payload.
     *
     * @param responseEntity response entity of type T
     */
    public void setResponseEntity(T responseEntity) {
        this.responseEntity = responseEntity;
    }
}
