package com.smu.csd.pokerivals;

import com.smu.csd.pokerivals.exception.MacInvalidException;
import com.smu.csd.pokerivals.record.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.NoSuchElementException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorMessage> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleAuthenticationError(AuthenticationException ex) {
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] trace = ex.getStackTrace();
        for (StackTraceElement traceElement : trace)
            builder.append("\tat ").append(traceElement).append("\n");
        log.error(builder.toString());
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorMessage> handleBadCredentials(BadCredentialsException ex) {
        return new ResponseEntity<>(new ErrorMessage(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({SQLIntegrityConstraintViolationException.class,SQLIntegrityConstraintViolationException.class})
    public ResponseEntity<ErrorMessage> handleJdbcSQLIntegrityConstraintViolation(){
        return new ResponseEntity<>(new ErrorMessage("Issue with data: Please ensure the account has not been used before and/or choose a new username"), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MacInvalidException.class)
    public ResponseEntity<ErrorMessage> handleMacInvalidException(MacInvalidException ex){
        return new ResponseEntity<>(new ErrorMessage("Link account email link is invalid! Please ask admin to resubmit"), HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorMessage> handleNoSuchElementException(NoSuchElementException ex){
        return new ResponseEntity<>(new ErrorMessage("Data not found"), HttpStatus.NOT_FOUND);
    }

}
