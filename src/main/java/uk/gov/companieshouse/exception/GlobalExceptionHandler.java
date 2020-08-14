package uk.gov.companieshouse.exception;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        LOGGER.info("[Unauthorised] - {}", request.getRequestURL().toString(), ex);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public void handleConflict(ConflictException ex, HttpServletRequest request) {
        LOGGER.info("[Conflict] - {}", request.getRequestURL().toString(), ex);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public void handleRuntime(RuntimeException ex, HttpServletRequest request) {
        LOGGER.error("[Internal Server Error] - {}", request.getRequestURL().toString(), ex);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public void handleNotFound(RuntimeException ex, HttpServletRequest request) {
        LOGGER.info("[Not Found] - {}", request.getRequestURL().toString());
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ServiceUnavailableException.class)
    public void handleServiceUnavailable(RuntimeException ex, HttpServletRequest request) {
        LOGGER.info("[Service unavailable] - {}", request.getRequestURL().toString(), ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public void handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        LOGGER.info("[Bad Request] - {}", request.getRequestURL().toString(), ex);
    }
}
