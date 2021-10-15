package com.nadir.estore.ProductsService.core.errorhandling;

import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class ProductsServiceErrorHandler {

    @ExceptionHandler(value = {IllegalStateException.class})
    public ResponseEntity<ErrorMessage> handleIllegaleStateException(IllegalStateException ex, WebRequest webRequest) {
        return getResponseEntity(ex);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorMessage> handleException(Exception ex, WebRequest webRequest) {
        return getResponseEntity(ex);
    }

    @ExceptionHandler(value = {CommandExecutionException.class})
    public ResponseEntity<ErrorMessage> handleCommandExecutionException(CommandExecutionException ex, WebRequest webRequest) {
        return getResponseEntity(ex);
    }

    private ResponseEntity<ErrorMessage> getResponseEntity(Exception ex) {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
