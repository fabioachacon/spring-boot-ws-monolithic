package com.parent.ws.app.exceptions;

import java.util.Date;

import com.parent.ws.app.exceptions.custom.ErrorMessage;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class AppExceptionsHandler {

    @ExceptionHandler(value = { UserServiceException.class })
    public ResponseEntity<Object> handleUserServiceException(UserServiceException exception, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), exception.getMessage());

        return new ResponseEntity<Object>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> handleOtherExceptions(Exception exception, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), exception.getMessage());

        return new ResponseEntity<Object>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
