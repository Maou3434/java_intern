package com.example.exceptions;

import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.response.ResponseClass;


/**
 * Global exception handler for REST API exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles database constraint violations.
     *
     * @return Response with HTTP 409 Conflict status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseClass<String> handleConstraintError() {
        logger.error("DataIntegrityViolationException");
        return new ResponseClass<>(
            HttpStatus.CONFLICT,
            "Field missing or violates constraints",
            null
        );
    }

    /**
     * Handles entity not found exceptions.
     *
     * @param ex the thrown EntityNotFoundException
     * @return Response with HTTP 404 Not Found status
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseClass<String> handleEntityNotFound(EntityNotFoundException ex) {
        logger.error("EntityNotFoundException: {}", ex.getMessage(), ex);
        return new ResponseClass<>(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            null
        );
    }

    /**
     * Handles unsupported media type exceptions.
     *
     * @return Response with HTTP 400 Bad Request status
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseClass<String> handleWrongInput() {
        logger.error("HttpMediaTypeNotSupportedException");
        return new ResponseClass<>(
            HttpStatus.BAD_REQUEST,
            "Wrong input format",
            null
        );
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param e the thrown IllegalArgumentException
     * @return Response with HTTP 400 Bad Request status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseClass<String> handleIllegalArg(IllegalArgumentException e) {
        logger.error("IllegalArgumentException: {}", e.getMessage(), e);
        return new ResponseClass<>(
            HttpStatus.BAD_REQUEST,
            e.getMessage(),
            null
        );
    }

    /**
     * Handles malformed JSON request exceptions.
     *
     * @param ex the thrown HttpMessageNotReadableException
     * @return Response with HTTP 400 Bad Request status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseClass<String> handleMalformedJson(HttpMessageNotReadableException ex) {
        logger.error("HttpMessageNotReadableException: {}", ex.getMessage(), ex);
        return new ResponseClass<>(
            HttpStatus.BAD_REQUEST,
            "Malformed JSON request",
            null
        );
    }

    /**
     * Handles method argument validation exceptions.
     *
     * @param ex the thrown MethodArgumentNotValidException
     * @return Response with detailed validation error messages and HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseClass<String> handleArgNotValid(MethodArgumentNotValidException ex) {
        logger.error("MethodArgumentNotValidException: {}", ex.getMessage(), ex);
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ResponseClass<>(
            HttpStatus.BAD_REQUEST,
            errorMessage,
            null
        );
    }

    /**
     * Handles illegal state exceptions.
     *
     * @param ex the thrown IllegalStateException
     * @return Response with HTTP 500 Internal Server Error status
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseClass<String> handleIllegalStateException(Exception ex) {
        logger.error("IllegalStateException: {}", ex.getMessage(), ex);
        return new ResponseClass<>(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            null
        );
    }

    /**
     * Handles all uncaught exceptions.
     *
     * @param ex the thrown Exception
     * @return Response with HTTP 500 Internal Server Error status and a generic message
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseClass<String> handleGenericException(Exception ex) {
        logger.error("Unhandled Exception: {}", ex.getMessage(), ex);
        return new ResponseClass<>(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred: " + ex.getMessage(),
            null
        );
    }
}
