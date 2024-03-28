package uk.gov.companieshouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.companieshouse.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger;

    public GlobalExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        final Map<String, String> validationErrors = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        logger.info(String.format("[Unprocessable entity] - %s - %s", request.getRequestURL().toString(), validationErrors));

        return validationErrors;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorisedException.class)
    public void handleUnauthorised(UnauthorisedException ex, HttpServletRequest request) {
        logger.info(String.format("[Unauthorised] - %s - %s", request.getRequestURL().toString(), ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public void handleConflict(ConflictException ex, HttpServletRequest request) {
        logger.info(String.format("[Conflict] - %s", request.getRequestURL().toString()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public void handleRuntime(RuntimeException ex, HttpServletRequest request) {
        logger.error(String.format("[Internal Server Error] - %s", request.getRequestURL().toString()), ex);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public void handleNotFound(RuntimeException ex, HttpServletRequest request) {
        logger.info(String.format("[Not Found] - %s", request.getRequestURL().toString()));
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ServiceUnavailableException.class)
    public void handleServiceUnavailable(RuntimeException ex, HttpServletRequest request) {
        logger.info(String.format("[Service unavailable] - %s", request.getRequestURL().toString()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public void handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        logger.info(String.format("[Bad Request] - %s - %s", request.getRequestURL().toString(), ex.getMessage()));
    }
}
