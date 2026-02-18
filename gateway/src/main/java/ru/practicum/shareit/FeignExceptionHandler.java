package ru.practicum.shareit;

import feign.FeignException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FeignExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<byte[]> handle(FeignException e) {
        int status = e.status();
        HttpStatusCode code = (status > 0) ? HttpStatusCode.valueOf(status) : HttpStatus.SERVICE_UNAVAILABLE;
        return new ResponseEntity<>(e.content(), new HttpHeaders(), code);
    }
}
