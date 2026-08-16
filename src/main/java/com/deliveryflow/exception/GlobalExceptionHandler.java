package com.deliveryflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Turns exceptions into the single {@link ErrorResponse} shape.
 *
 * <p>The status codes are deliberately distinct: 400 means the request itself was malformed,
 * 404 means the entity does not exist, and 409 means the request was understood but the
 * domain refused it (busy driver, illegal transition, duplicate key).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** Bean Validation failures, reported per field so the UI can highlight the input. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ErrorResponse body = ErrorResponse.withFields(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Covers an unparseable body and, importantly, an unknown enum constant such as
     * {@code {"status": "FLYING"}}, which Jackson rejects before validation runs.
     *
     * <p>Jackson's own message for that case exposes package names, stream offsets and the
     * internal reference chain, so an invalid enum is reformatted into a message that names
     * the rejected value and lists what the API will accept.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        InvalidFormatException ife = findInvalidFormat(ex);

        if (ife != null && ife.getTargetType() != null && ife.getTargetType().isEnum()) {

            String accepted = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(constant -> ((Enum<?>) constant).name())
                    .collect(Collectors.joining(", "));

            return build(HttpStatus.BAD_REQUEST,
                    "'%s' is not a valid %s. Accepted values: %s"
                            .formatted(ife.getValue(), ife.getTargetType().getSimpleName(), accepted),
                    request);
        }

        return build(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Parameter '%s' has an invalid value: %s".formatted(ex.getName(), ex.getValue()),
                request);
    }

    /** An unmapped URL should be a plain 404, not a 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "No endpoint " + request.getRequestURI(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error: " + ex.getMessage(), request);
    }

    /**
     * Jackson nests the real failure several levels down — binding a record component wraps
     * it again — so the cause chain is walked rather than inspecting only the direct cause.
     */
    private InvalidFormatException findInvalidFormat(Throwable throwable) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t instanceof InvalidFormatException ife) {
                return ife;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return null;
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                 HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                ErrorResponse.of(status.value(), status.getReasonPhrase(), message,
                        request.getRequestURI()));
    }
}
