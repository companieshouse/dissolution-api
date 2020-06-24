package uk.gov.companieshouse.controller;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.UnauthorisedException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionHandlerController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ExceptionHandlerController.class);

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        final Map<String, String> validationErrors = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        LOGGER.info("[Unprocessable entity] - {} - {}", request.getRequestURL().toString(), validationErrors);

        return validationErrors;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorisedException.class)
    public void handleUnauthorised(UnauthorisedException ex, HttpServletRequest request) {
        LOGGER.info("[Unauthorised] - {}", request.getRequestURL().toString());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public void handleConflict(ConflictException ex, HttpServletRequest request) {
        LOGGER.info("[Conflict] - {}", request.getRequestURL().toString());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public void handleRuntime(RuntimeException ex, HttpServletRequest request) {
        LOGGER.error("[Internal server error] - {}", request.getRequestURL().toString(), ex);
    }
}
