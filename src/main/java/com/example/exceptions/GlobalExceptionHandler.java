package com.example.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.response.ResponseClass;

/**
 * Global exception handler for REST API exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles database constraint violations.
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
     * @param ex EntityNotFoundException
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
     * @param e IllegalArgumentException
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
     * @param ex HttpMessageNotReadableException
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
    
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseClass<String> handleArgNotValid(MethodArgumentNotValidException ex){
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
    
    
    @ExceptionHandler(IllegalStateException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseClass<String> handleIllegalStateException(Exception ex) {
    	logger.error("Exception: {}", ex.getMessage(), ex);
        return new ResponseClass<>(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred: " + ex.getMessage(),
            null
        );
    }
    
    
    /**
     * Handles generic exceptions.
     *
     * @param ex Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseClass<String> handleGenericException(Exception ex) {
    	logger.error("Exception: {}", ex.getMessage(), ex);
        return new ResponseClass<>(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred: " + ex.getMessage(),
            null
        );
    }
}
