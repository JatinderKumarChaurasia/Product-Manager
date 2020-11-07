package com.jkc.microservices.util.http;

import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalControllerExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody
    HttpErrorInfo handleNotFoundExceptions(ServerHttpRequest serverHttpRequest, Exception exception) {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, serverHttpRequest, exception);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody
    HttpErrorInfo handleInvalidInputException(ServerHttpRequest serverHttpRequest, Exception exception) {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, serverHttpRequest, exception);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(InternalServerException.class)
    public @ResponseBody
    HttpErrorInfo handleInternalServerException(ServerHttpRequest serverHttpRequest, Exception exception) {
        LOG.debug("handling internal server error");
        return createHttpErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, serverHttpRequest, exception);
    }

    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest serverHttpRequest, Exception exception) {
        final String path = serverHttpRequest.getPath().pathWithinApplication().value();
        final String exceptionMessage = exception.getMessage();
        LOG.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, exceptionMessage);
        return new HttpErrorInfo(httpStatus, path, exceptionMessage);
    }
}
